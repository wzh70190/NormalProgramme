简单的Java代理：

## java探针1.0版本
用test1和test2
最基础的探针

在idea的VM_OPTIONS后加上这个。
-javaagent:D:\code\javaagent\out\artifacts\unnamed2/unnamed.jar=Hello 

### 简单版

即可看出效果


需要将其变成jar

### 实现字节码切面

打成jar包需要注意。按照图中打包
![sss](https://gitee.com/sss1235/picture/raw/master/QQ图片20190820011532.png)

升级版是myageny，myprogram