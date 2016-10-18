//
// Created by mengyouxu on 2016/10/14.
//

#include "UdpDataSource.h"

#include <arpa/inet.h>
#include <unistd.h>


#include <android/log.h>
#define LOG_TAG "NativeUdpDataSource"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

UdpCacher::UdpCacher(std::string addr, int port){
    mUdpAddress = addr;
    mUdpPort = port;
    mNeedDownload = true;
    mIsSocketInited = false;
    mBytePoolSize = 4096 * 1024;
    mBytePool = new DataPacket((char*)malloc(mBytePoolSize), 0);

}
UdpCacher::~UdpCacher() {
    if(mBytePool != nullptr){
        delete mBytePool;
    }
    LOGD("L%d UdpCacher destructor", __LINE__);
}
int UdpCacher::initUdpConnection(std::string addr, int port){
    mSocketMaxRcvBufferSize = 8192 * 10;
    int ret_val = 0;
    struct sockaddr_in localAddr;
    memset(&localAddr, 0, sizeof(struct sockaddr_in));
    localAddr.sin_family = AF_INET;
    localAddr.sin_port = htons((unsigned short)port);
    localAddr.sin_addr.s_addr = inet_addr(addr.c_str());// htonl(INADDR_ANY);
    LOGD("L%d initUdpConnection enter %s:%d", __LINE__, addr.c_str(),port);
    mSocket = socket(AF_INET, SOCK_DGRAM, 0);
    if(mSocket < 0){
        LOGD("L%d error create socket fail", __LINE__);
        return -1;
    }

    ret_val = bind(mSocket, (struct sockaddr *)&localAddr, sizeof(struct sockaddr_in));
    if(ret_val < 0){
        LOGD("L%d error bind fail err[%d]:%s", __LINE__,  errno, strerror(errno));
        return -1;
    }

    struct timeval tv;
    tv.tv_sec = 1;
    tv.tv_usec = 0;
    ret_val = setsockopt(mSocket, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(struct timeval));
    if(ret_val < 0){
        LOGD("L%d error SO_RCVTIMEO err[%d]:%s", __LINE__,  errno, strerror(errno));
        return -1;
    }
    //ret_val = setsockopt(mSocket, SOL_SOCKET, SO_SNDTIMEO, &tv, sizeof(tv));

    ret_val = setsockopt(mSocket, SOL_SOCKET, SO_RCVBUF, (char *)&mSocketMaxRcvBufferSize, sizeof(int));
    if(ret_val < 0){
        LOGD("L%d error SO_RCVBUF fail", __LINE__);
        return -1;
    }
    mIsSocketInited = true;
    LOGD("L%d initUdpConnection leave", __LINE__);
    return 0;
}

int UdpCacher::cacherStart() {
    LOGD("L%d cacherStart enter", __LINE__);
    initUdpConnection(mUdpAddress, mUdpPort);
    mBytePool->mSize = 0;
    mBytePool->mOffset = 0;
    memset(mBytePool->mData, 0, mBytePoolSize);
    mNeedDownload = true;
    mDownloadThread = new std::thread(&UdpCacher::downloader,this);
    LOGD("L%d cacherStart leave", __LINE__);
    return 0;
}

int UdpCacher::cacherStop() {
    LOGD("L%d cacherStop leave", __LINE__);
    mNeedDownload = false;
    if(mDownloadThread != nullptr) {
        mDownloadThread->join();
    }
    if(mSocket >= 0){
        ::close(mSocket);
    }
    LOGD("L%d cacherStop leave", __LINE__);
    return 0;
}

int UdpCacher::queueBuffer(DataPacket *packet){
    DataPacket *recvBuff = nullptr;
    if(packet != nullptr){
        mQMutex.lock();
        mBuffQueue.push(packet);
        mQMutex.unlock();
    }

    if(mBuffQueue.size() > 1024){
        LOGD("L%d clear mBufferQueue, current size = %d", __LINE__, mBuffQueue.size());
        for(int i = 0; i < 512; i++){
            mQMutex.lock();
            recvBuff = mBuffQueue.front();
            if(recvBuff != nullptr) delete recvBuff;
            mBuffQueue.pop();
            mQMutex.unlock();
        }
        LOGD("L%d mBufferQueue.size() = %d", __LINE__, mBuffQueue.size());
    }
    return mBuffQueue.size();
}

int UdpCacher::dequeueBuffer(DataPacket **packet) {
    DataPacket *pPacket = nullptr;
    mQMutex.lock();
    if (mBuffQueue.size() > 0) {
        pPacket = mBuffQueue.front();
        mBuffQueue.pop();
    }
    mQMutex.unlock();
    if(pPacket != nullptr) {
        *packet = pPacket;
        return pPacket->mSize;
    } else {
        return 0;
    }
}

int UdpCacher::read(char *buff, int size) {
    DataPacket *tmpBuff = nullptr;
    int ret_val = 0;
    do {
        if (mBytePoolSize - mBytePool->mSize > mBytePoolSize / 4) {
            dequeueBuffer(&tmpBuff);
            if(tmpBuff == nullptr) return 0;
            memcpy(mBytePool->mData + mBytePool->mSize, tmpBuff->mData, tmpBuff->mSize);
            mBytePool->mSize += tmpBuff->mSize;
            delete tmpBuff;
        } else {
            if (mBytePool->mSize - mBytePool->mOffset < mBytePoolSize / 8) {
                LOGD("L%d read pool size %d, offset:%d", __LINE__, mBytePool->mSize, mBytePool->mOffset);
                memcpy(mBytePool->mData, mBytePool->mData + mBytePool->mOffset,
                       mBytePool->mSize - mBytePool->mOffset);
                mBytePool->mSize = mBytePool->mSize - mBytePool->mOffset;
                mBytePool->mOffset = 0;
                LOGD("L%d read pool size %d, offset:%d", __LINE__, mBytePool->mSize, mBytePool->mOffset);
            }
        }
    }while(false);

    if(mBytePool->mSize - mBytePool->mOffset >= size){
        memcpy(buff, mBytePool->mData + mBytePool->mOffset, size);
        mBytePool->mOffset += size;
        ret_val = size;
    }
    return ret_val;
}
void UdpCacher::downloader() {
    struct sockaddr_in addr;
    int ret_val = 0;
    int recvBufferLen = 4096;
    int udpPacketCount = 1;
    char *recvBuff = nullptr;

    int sockAddrLen = sizeof(struct sockaddr_in);
    LOGD("L%d downloader enter", __LINE__);

    if(mIsSocketInited == false){
        LOGD("L%d socket not init", __LINE__);
        return;
    }

    while (mNeedDownload == true) {
        recvBuff = (char *) malloc(recvBufferLen);
        ret_val = recvfrom(mSocket, recvBuff, recvBufferLen, 0, (struct sockaddr *) &addr,
                           &sockAddrLen);
        //if (udpPacketCount % 100 == 0) {
        //    LOGD("L%d udpPacketCount :%d, q size:%d", __LINE__, udpPacketCount, mBuffQueue.size());
        //}
        if (ret_val > 0) {
            udpPacketCount++;
            DataPacket *packet = new DataPacket(recvBuff, ret_val);
            queueBuffer(packet);
        } else {
            LOGD("L%d recvfrom return %d, errno[%d]:%s", __LINE__, ret_val, errno, strerror(errno));
        }
    }

    // Clear buffer
    DataPacket *tp = nullptr;
    mQMutex.lock();
    while(mBuffQueue.size() > 0) {
        tp = mBuffQueue.front();
        if (recvBuff != nullptr) delete tp;
        mBuffQueue.pop();
    }
    mQMutex.unlock();
    LOGD("L%d downloader leave", __LINE__);
}

