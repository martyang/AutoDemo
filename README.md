### AutoDemo

由于导入了系统的所有框架代码，所以在编译的时候会出现64K的问题，具体的配置查看这篇文章。

[Configure Apps with Over 64K Methods(Android项目方法数超过64K的配置)](https://vikingden.cn/2017/03/28/android-android-notes-035-configure-apps-with-over-64k-methods/)

### 视频录制功能

**注意：视频录制功能只适应于Android5.0版本以上的系统，如果低于Android5.0，会自动忽略**。

代码更新点：

```sh
app
  -manifests
    -AndroidManifest.xml
  -java
    -com
      -gionee
	 -demo
	   -ScreenConstant.java
           -ScreenRecorderActivity.java
	   -ScreenRecordService.java
  -res
    -layout
	-layout_screen_recorder.xml
    -values
	-strings.xml

```

视频的录制工作主要是通过ScreenRecordService实现，由于Android的安全机制，在启动录制时要权限申请，必须要在Activity中实现。所以这里主要是通过先启动Activity在启动Service的方式来执行。

#### **如何执行**

##### 开始录制

```sh
adb shell am start -n com.gionee.demo/com.gionee.demo.ScreenRecorderActivity --ei type 0
-e name your_file_name -e path your_save_path
```

可以在代码中集成上述命令，下面就各个参数进行介绍

* --ei type 0　		　: 指定整型的type参数，0代表录制开始，1代表录制结束；
* -e name your_file_name 　　: 指定字符型的name参数，代表要保存的视频名称，比如Email_1000；
* -e path　your_save_path　　 :　指定字符型的path参数，代表要保存的视频存放路径，比如/mnt/sdcard/Download/.
可以不指定path，默认的路径为/mnt/sdcard/dongzhou/，视频文件会生成在这个路径下的videos目录下。

##### 停止录制

```sh
adb shell am start -n com.gionee.demo/com.gionee.demo.ScreenRecorderActivity --ei type 1
-e name your_file_name --ez result true
```

* --ei type 0　		　: 指定整型的type参数，0代表录制开始，1代表录制结束；
* -e name your_file_name 　　: 指定字符型的name参数，代表要保存的视频名称，必须和开始时指定的一致；
* --ez result true　　　　　　　　　: 指定boolean类型的参数，true代表当前的case执行成功，false代表失败;

**注意：由于之前的case使用的是东周提供的继承父类BaseTestCase，这边无法统一集成上述的视频录制功能，需在需要加入视频
功能的case上自己手动加入，具体的加入可以在case执行开始之前启动开始录制命令，并且确保在case完成之后取消视频录制**。

**注意：如果使用的common-lib进行的case编写，也就是case类继承自GioneeTestCase,则已在case执行开始和结束加入了自动
开启视频录制的功能，并case执行成功后自动删除视频，执行失败则会保存**。

以下是在GioneeTestCase中已添加的代码段:

```java
private void onScreenRecordStart(){
  try {
      String command = "am start -n com.gionee.demo/com.gionee.demo.ScreenRecorderActivity --ei type 0 -e name "
            + caseName + " -e path /mnt/sdcard/dongzhou/" ;
      Util.d("start command : " + command);
      mDevice.executeShellCommand(command) ;
  } catch (IOException e) {
      e.printStackTrace();
  }
}

private void onScreenRecordStop(boolean isFail){
  try {
      String command = "am start -n com.gionee.demo/com.gionee.demo.ScreenRecorderActivity --ei type 1 -e name "
            + caseName + " --ez result " + !isFail ;
      Util.d("stop command : " + command);
      mDevice.executeShellCommand(command) ;
  } catch (IOException e) {
      e.printStackTrace();
  }
}
```

#### **视频大小**

视频的大小和当前的操作有关，如果是静止的视频录制，文件大小比较小，测试如下：

| 文件名称    | 测试时间   |  大小  |
| --------   | -----:   | :----: |
| Filemanager_1000.mp4        | 22s      |   4.0M    |
| test_fail_long_time.mp4        | 33m49s(主要是静止状态下)      |   1.9M    |
| test_fail.mp4        | 1m37s(模拟一直在滑屏操作)      |   7.8M    |
| test_success.mp4        | 5m13(模拟随机操作)      |   4.0M    |

具体的大小如下截图：

<div  align="center">
<img src="/dengwj/AutoDemo/raw/master/screenshots/20170719-134523.png" width = "360" height = "640" alt="screenshot for size" align=center />
</div>


关于MediaProjectionManager的使用可以参考官方的示例，[MediaProjectionDemo.java](https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/media/projection/MediaProjectionDemo.java)
