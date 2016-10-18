//
// Created by mengyouxu on 2016/10/14.
//

#ifndef FILEBROWSER4ANDROID_UDPDATASOURCE_H
#define FILEBROWSER4ANDROID_UDPDATASOURCE_H

#include <thread>
#include <queue>
#include <mutex>

#include <netinet/in.h>
#include <sys/socket.h>

class UdpCacher{
  public:
    class DataPacket{
      public:
        char *mData;
        int mSize;
        int mOffset;
        DataPacket(char *data, int size){
            mData = data;
            mSize = size;
            mOffset = 0;
        }
        ~DataPacket(){
            if(mData != nullptr){
                free(mData);
            }
        }
    };
    UdpCacher(std::string addr, int port);
    ~UdpCacher();

    int cacherStart();
    int cacherStop();

    int queueBuffer(DataPacket *buff);

    /*
     *  dequeueBuffer
     *   buff : you must delete buff after use
     */
    int dequeueBuffer(DataPacket **buff);
    int read(char *buff, int size);

  private:
    bool mNeedDownload;
    bool mIsSocketInited;
    int mUdpPort;
    int mSocket;
    int mSocketMaxRcvBufferSize;
    DataPacket *mBytePool;
    int mBytePoolSize;

    std::string mUdpAddress;
    std::thread *mDownloadThread;
    std::queue<DataPacket *> mBuffQueue;
    std::mutex mQMutex;

    int initUdpConnection(std::string addr, int port);
    void downloader();
};

#endif //FILEBROWSER4ANDROID_UDPDATASOURCE_H
