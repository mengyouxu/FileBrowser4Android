# FileBrowser4Android
#
# This is a test purpose project.
# You can play media file by:
#    1. android.media.MediaPlayer
#    2. android.media.MediaCodec/android.media.MediaExtractor
#    3. ExoPlayer
#
#   Note:
#   If you want to play some special uri, like:
#       "/storage/sdcard/playlist.m3u8"
#       "http://192.168.1.1/playlist.m3u8"
#       "udp://192.168.1.1:1234"
#   You can put the uri string into a "*.ulist" file. Then click it
#   in FileBrowser.
#
[2016-08-16]
	Now I can play media file by MediaPlayer or  MediaExtractor+MediaCodec;
	To be done : using ExoPlayer

[2016-08-17]
    1. Change project directory structor to Android Studio style.
    2. ExoPlayerActivity can play mp3.

[2016-08-22]
    1. Play udp://xxxx:yy with ExoPlayer
    2. Fix issue: FileBrowserAcitvity crash when press Back key.

[2016-09-22]
    1. Rewrite UdpDataSource -> UdpDataSource2. NOT WORK WELL NOW.
    2. Add Scheme "udp2" for testing(udp2://xxxx:xx -> UdpDataSource2)
