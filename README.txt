说明:
    此APK 主要用来测试Android 播放器的行为，针对的是Android 4.4或更高的版本.

编译好的Apk下载地址:
    链接: https://pan.baidu.com/s/1-AQKmC9Yt6v1s_zE0L2jjQ 提取码: dn3x

    编译使用AndroidStudio 2.2.1.
    \app\libs\ExoPlayer-r1.5.8.jar 这个是编译好的ExoPlayer库，可以直接使用里面的方法.

    安装后的入口Activity 是FileBrowserActivity，显示名称 FileBrowser，该Activity会首先通过
getExternalStorageDirectory() 访问ExternelStorage，用ListView显示文件列表.

    为了方便测试，FileBrowserActivity可以读取 “.ulist”后缀的text文件，将其第一行内容
作为媒体文件连接发送给VideoPlayerActivity去播放. 比如：
    echo "http://192.168.1.101/files/playlist.m3u8" > /sdcard/m3u8.ulist
    在FileBrowser中点击m3u8.ulist文件的时候，VideoPlayerActivity就会启动,
"http://192.168.1.101/files/playlist.m3u8" 就是VideoPlayer 要播放的片源地址.

    VideoPlayerActivity启动后，会提示 当前可以用三种方式播放媒体:
        1. android.media.MediaPlayer 接口最简单，目前使用最广泛
        2. android.media.MediaCodec  需要配合MediaExtractor使用，本Apk现在只能用这种方法播放Video，
           Audio会被忽略
        3. ExoPlayer   使用比较复杂，但功能很丰富.

.ulist 举例:
    1. http://192.168.1.101/files/playlist.m3u8   -->播放hls媒体
    2. /storage/sdcard/abc.mp4  -->播放本地文件abc.mp4

    3. udp://192.168.1.101:1234  -->播放udp stream, 192.168.1.101是本机地址，127.0.0.1无法使用，
        目前还没查到原因
    4. udp3://192.168.1.101:1234  -->此方法可以指定 使用native UdpDataSource播放udp stream，
        相对java UdpDataSource，丢包大大减少
