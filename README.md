# Dune Interactive Segmentation

基于 Python 深度学习模型（SimpleClick）与 Android 移动端的沙丘图像交互式分割应用

本项目使用了SimpleClick开源项目作为模型后端，原项目链接如下：

https://github.com/uncbiag/SimpleClick

请将本项目使用的模型端文件添加或替换到原始的SimpleClick项目中，相关链接如下：

https://github.com/warmrid233/dune_seg_model

## 演示
下载demo_video.mp4和README.md到本地即可观看演示视频
<video controls src="demo_video.mp4" title="Title"></video>

## 目录
- [项目概述](#项目概述)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [使用指南](#使用指南)
- [开发文档](#开发文档)
- [贡献指南](#贡献指南)
- [许可协议](#许可协议)

## 项目概述
基于 Android 移动端与 Python 深度学习后端实现的沙丘地貌交互式分割系统，主要功能包括：

- 📱 Android 端功能：
  - 本地图像采集
  - 交互式分割标注
  - 结果可视化与导出
- 🖥️ 后端功能：
  - 基于 [SimpleClick] 的语义分割
  - 交互式掩模优化

**技术栈**：
- 后端：Python [3.8+] + flask + PyTorch
- 移动端：Android [14.0(API-34)+]


## 环境要求
**后端服务**:
- PyCharm 2023.3.1+

- Python 3.8+

- PyTorch 2.4.1+

- CUDA [12.7] (GPU 加速推荐)(根据gpu选择版本)

依赖安装：

```bash
pip install -r requirements.txt
```

**Android 端**:

- Android Studio 2023.3+

- Android SDK 34+

- Java jdk 17+

- Gradle 8.6+

- 设备要求：Android 14.0+，支持 Camera2 API

## 快速开始

**后端服务启动**
- 在PyCharm中运行，运行/调试配置如下：
~~~bash
script: 
*/SimpleClick-1.0/interactive_demo/android_demo/android_control.py

脚本形参： 
--checkpoint=./weights/simpleclick_models/dune_seg_model.pth --gpu 0
~~~

**Android 应用构建**

- 用 Android Studio 打开 interactive_segment 的 app 目录

- 为项目配置相应的gradle与java版本

- 连接设备或使用模拟器运行

## 使用指南
**交互流程**

1. 启动 Android 应用与模型后端

2. 输入模型端ip与端口，进行连接测试

3. 进入应用主界面，从图库选择图像来源上传

4. 进入交互界面，使用左侧按钮进行操作选择（调整图片，点击交互，连接线，画笔，撤回，完成交互，保存退出）

5. 选择保存退出后，回到主界面，保存交互后的图片到图库

**手势操作**

1. 调整图片时：
    - 单指/触控笔拖动：平移图像
    - 双指捏合/扩张：缩放图像

2. 点击交互时：
    - 单指/触控笔点击：添加兴趣点/非兴趣点

3. 连接线交互时:
    - 单指单指/触控笔点击：添加连接线结点，绘制连接线

4. 画笔交互时：
    - 单指/触控笔拖动：绘制线条

5. 撤回/完成交互/保存退出：点击相应按钮即可完成操作

## 开发文档

**模型训练**
- 模型训练相关方法可参考SimpleClick项目描述，链接：
https://github.com/uncbiag/SimpleClick

**API 接口规范**
|端点           |方法           |参数格式            |功能               |
|:---           |:---           |:---               |:---              |
|/test          |GET            |null               |测试android端与模型端链接|
|/load_img      |POST           |FormData(image: File, name: String, type: String)|android端上传图片|
|/click         |POST           |FormData(x: Float, y: Float, flag: Int)          |android端上传点击交互数据，执行分割|
|/get_image     |GET            |null               |android端获取交互后的图片|
|/undo          |GET            |null               |执行撤回分割操作|
|/finish        |GET            |null               |执行分割结果保存操作|

## 贡献指南
1. 欢迎通过 Issue 或 Pull Request 参与贡献，请遵循：

2. 新建功能分支：git checkout -b feature/your-feature

3. 提交清晰 commit 信息

4. 确保 Android 与 Python 代码风格统一
			
## 许可协议

Apache 2.0 © 2025 [康昊楠, ]

## 注意事项：

- 确保 Android 设备与后端服务器处于同一局域网

- 推荐使用 RTX 3060 及以上 GPU 加速推理

- 训练数据需包含多样化的沙丘地貌样本
