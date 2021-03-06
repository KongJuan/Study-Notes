# 1.File类的使用

## 1.1概述

 java.io.File类：文件和文件目录路径的抽象表示形式，与平台无关

 File 能新建、删除、重命名文件和目录，但 File 不能访问文件内容本身。如果需要访问文件内容本身，则需要使用输入/输出流。 

 想要在Java程序中表示一个真实存在的文件或目录，那么必须有一个File对 象，但是Java程序中的一个File对象，可能没有一个真实存在的文件或目录。 

 File对象可以作为参数传递给流的构造器

## 1.2常用构造器

- **public File(String pathname)** 

  以pathname为路径创建File对象，可以是**绝对路径或者相对路径**，如果pathname是相对路径，则默认的当前路径在系统属性user.dir中存储。

  - 绝对路径：是一个固定的路径,从盘符开始
  - 相对路径：是相对于某个位置开始 

- **public File(String parent,String child)**

  以parent为父路径，child为子路径创建File对象。 

- **public File(File parent,String child)**

  根据一个父File对象和子文件路径创建File对象

**路径分隔符**

 路径中的每级目录之间用一个**路径分隔符**隔开。

 路径分隔符和系统有关：

 windows和DOS系统默认使用“\”来表示

 UNIX和URL使用“/”来表示

 Java程序支持跨平台运行，因此路径分隔符要慎用。

 为了解决这个隐患，File类提供了一个常量：

​             **public static final String separator**。根据操作系统，动态的提供分隔符。

```java
File file1 = new File("d:\\atguigu\\info.txt");
File file2 = new File("d:" + File.separator + "atguigu" + File.separator + "info.txt");
File file3 = new File("d:/atguigu");
```

![image-20200811214847671](IO流、序列化.assets/image-20200811214847671.png)

## 1.3常用方法

**File类的获取功能**

 `public String getAbsolutePath()`：获取绝对路径

 `public String getPath()` ：获取路径

 `public String getName()` ：获取名称

 `public String getParent()`：获取上层文件目录路径。若无，返回null

 `public long length()` ：获取文件长度（即：字节数）。不能获取目录的长度。 

 `public long lastModified()` ：获取最后一次的修改时间，毫秒值

 `public String[] list()` ：获取指定目录下的所有文件或者文件目录的名称数组

 `public File[] listFiles()` ：获取指定目录下的所有文件或者文件目录的File数组

**File类的重命名功能**

 `public boolean renameTo(File dest)`:把文件重命名为指定的文件路径

**File类的判断功能**

 `public boolean isDirectory()`：判断是否是文件目录

 `public boolean isFile()` ：判断是否是文件

 `public boolean exists()` ：判断是否存在

 `public boolean canRead()` ：判断是否可读

 `public boolean canWrite()` ：判断是否可写

 `public boolean isHidden()` ：判断是否隐藏

**File类的创建功能**

 `public boolean createNewFile()` ：创建文件。若文件存在，则不创建，返回false

 `public boolean mkdir()` ：创建文件目录。如果此文件目录存在，就不创建了。如果此文件目录的上层目录不存在，也不创建。 

 `public boolean mkdirs()` ：创建文件目录。如果上层文件目录不存在，一并创建

> **注意事项：如果你创建文件或者文件目录没有写盘符路径，那么，默认在项目路径下。** 

```java
File dir1 = new File("D:/IOTest/dir1");
if (!dir1.exists()) { // 如果D:/IOTest/dir1不存在，就创建为目录
	dir1.mkdir();
}
// 创建以dir1为父目录,名为"dir2"的File对象
File dir2 = new File(dir1, "dir2");
if (!dir2.exists()) { // 如果还不存在，就创建为目录
	dir2.mkdirs();
}
File dir4 = new File(dir1, "dir3/dir4");
if (!dir4.exists()) {
	dir4.mkdirs();
}
// 创建以dir2为父目录,名为"test.txt"的File对象
File file = new File(dir2, "test.txt");
if (!file.exists()) { // 如果还不存在，就创建为文件
	file.createNewFile();
}
```

**File类的删除功能**

 `public boolean delete()`：删除文件或者文件夹

> 删除注意事项：
>
> Java中的删除不走**回收站**。 
>
> 要删除一个文件目录，请注意该文件目录内不能包含文件或者文件目录1

# 2.IO流原理及流的分类

## 2.1 IO流原理

- I/O是Input/Output的缩写， I/O技术是非常实用的技术，用于**处理设备之间的数据传输**。如读/写文件，网络通讯等。

- Java程序中，对于数据的输入/输出操作以“流(stream) “的方式进行。

- java.io包下提供了各种“流”类和接口，用以获取不同种类的数据，并通过**标准的方法**输入或输出数据。

**输入input**：读取外部数据（磁盘、光盘等存储设备的数据）到程序（内存）中。

**输出output**：将程序（内存）数据输出到磁盘、光盘等存储设备中

- I/O流体系

  ![image-20200920172608945](IO%E6%B5%81%E3%80%81%E5%BA%8F%E5%88%97%E5%8C%96.assets/image-20200920172608945.png)

## 2.2流的分类

按操作**数据单位**不同分为：**字节流(8 bit)**，**字符流(16 bit)**

按数据流的**流向**不同分为：**输入流，输出流**

按流的**角色**的不同分为：**节点流，处理流**

1. Java的IO流共涉及40多个类，实际上非常规则，都是从如下4个抽象基类派生的。

![image-20200812212440283](IO流、序列化.assets/image-20200812212440283.png)

2. 由这四个类派生出来的子类名称都是以其父类名作为子类名后缀

3. **节点流和处理流**

   - 节点流：直接从数据源或目的地读写数据

     ![image-20200812212638391](IO流、序列化.assets/image-20200812212638391.png)

   - 处理流：不直接连接到数据源或目的地，而是“连接”在已存在的流（节点流或处理流）之上，通过对数据的处理为程序提供更为强大的读写功能

     ![image-20200812212831253](IO流、序列化.assets/image-20200812212831253.png)



# 3.节点流（或文件流）

一切文件数据(文本、图片、视频等)在存储时，都是以二进制数字的形式保存，都一个一个的字节，那么传输时一样如此。所以，字节流可以传输任意文件数据。在操作流的时候，我们要时刻明确，无论使用什么样的流对象，底层传输的始终为二进制数据。

## 3.1字节输出流【OutputStream】

`java.io.OutputStream `抽象类是表示字节输出流的所有类的超类，将指定的字节信息写出到目的地。它定义了字节输出流的基本共性功能方法。

* `public void close()` ：关闭此输出流并释放与此流相关联的任何系统资源。  
* `public void flush() ` ：刷新此输出流并强制任何缓冲的输出字节被写出。  
* `public void write(byte[] b)`：将 b.length字节从指定的字节数组写入此输出流。  
* `public void write(byte[] b, int off, int len)` ：从指定的字节数组写入 len字节，从偏移量 off开始输出到此输出流。  
* `public abstract void write(int b)` ：将指定的字节输出流。

> 小贴士：
>
> close方法，当完成流的操作时，必须调用此方法，释放系统资源。

### **3.1.1文件输出流FileOutputStream类**

`java.io.FileOutputStream `类是文件输出流，用于将数据写出到文件。

**构造方法**

* `public FileOutputStream(File file)`：创建文件输出流以写入由指定的 File对象表示的文件。 
* `public FileOutputStream(String name)`： 创建文件输出流以指定的名称写入文件。  

当你创建一个流对象时，必须传入一个文件路径。**该路径下，如果没有这个文件，会创建该文件。如果有这个文件，会清空这个文件的数据。**

* 构造举例，代码如下：

```java
public class FileOutputStreamConstructor throws IOException {
    public static void main(String[] args) {
   	 	// 使用File对象创建流对象
        File file = new File("a.txt");
        FileOutputStream fos = new FileOutputStream(file);
      
        // 使用文件名称创建流对象
        FileOutputStream fos = new FileOutputStream("b.txt");
    }
}
```

**写出字节数据**

1. **写出字节**：`write(int b)` 方法，每次可以写出一个字节数据，代码使用演示：

```java
public class FOSWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileOutputStream fos = new FileOutputStream("fos.txt");     
      	// 写出数据
      	fos.write(97); // 写出第1个字节
      	fos.write(98); // 写出第2个字节
      	fos.write(99); // 写出第3个字节
      	// 关闭资源
        fos.close();
    }
}
输出结果：
abc
```

> 小贴士：
>
> 1. 虽然参数为int类型四个字节，但是只会保留一个字节的信息写出。
> 2. 流操作完毕后，必须释放系统资源，调用close方法，千万记得。

2. **写出字节数组**：`write(byte[] b)`，每次可以写出数组中的数据，代码使用演示：

```java
public class FOSWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileOutputStream fos = new FileOutputStream("fos.txt");     
      	// 字符串转换为字节数组
      	byte[] b = "黑马程序员".getBytes();
      	// 写出字节数组数据
      	fos.write(b);
      	// 关闭资源
        fos.close();
    }
}
输出结果：
黑马程序员
```

3. **写出指定长度字节数组**：`write(byte[] b, int off, int len)` ,每次写出从off索引开始，len个字节，代码使用演示：

```java
public class FOSWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileOutputStream fos = new FileOutputStream("fos.txt");     
      	// 字符串转换为字节数组
      	byte[] b = "abcde".getBytes();
		// 写出从索引2开始，2个字节。索引2是c，两个字节，也就是cd。
        fos.write(b,2,2);
      	// 关闭资源
        fos.close();
    }
}
输出结果：
cd
```

**数据追加续写**

经过以上的演示，每次程序运行，创建输出流对象，都会清空目标文件中的数据。如何保留目标文件中数据，还能继续添加新数据呢？

- `public FileOutputStream(File file, boolean append)`： 创建文件输出流以写入由指定的 File对象表示的文件。  
- `public FileOutputStream(String name, boolean append)`： 创建文件输出流以指定的名称写入文件。  

这两个构造方法，参数中都需要传入一个boolean类型的值，`true` 表示追加数据，`false` 表示清空原有数据。这样创建的输出流对象，就可以指定是否追加续写了，代码使用演示：

```java
public class FOSWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileOutputStream fos = new FileOutputStream("fos.txt"，true);     
      	// 字符串转换为字节数组
      	byte[] b = "abcde".getBytes();
		// 写出从索引2开始，2个字节。索引2是c，两个字节，也就是cd。
        fos.write(b);
      	// 关闭资源
        fos.close();
    }
}
文件操作前：cd
文件操作后：cdabcde
```

**写出换行**

Windows系统里，换行符号是`\r\n` 。把以指定是否追加续写了，代码使用演示：

```java
public class FOSWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileOutputStream fos = new FileOutputStream("fos.txt");  
      	// 定义字节数组
      	byte[] words = {97,98,99,100,101};
      	// 遍历数组
        for (int i = 0; i < words.length; i++) {
          	// 写出一个字节
            fos.write(words[i]);
          	// 写出一个换行, 换行符号转成数组写出
            fos.write("\r\n".getBytes());
        }
      	// 关闭资源
        fos.close();
    }
}

输出结果：
a
b
c
d
e
```

> * 回车符`\r`和换行符`\n` ：
>   * 回车符：回到一行的开头（return）。
>   * 换行符：下一行（newline）。
> * 系统中的换行：
>   * Windows系统里，每行结尾是 `回车+换行` ，即`\r\n`；
>   * Unix系统里，每行结尾只有 `换行` ，即`\n`；
>   * Mac系统里，每行结尾是 `回车` ，即`\r`。从 Mac OS X开始与Linux统一。

## 3.2字节输入流【InputStream】

`java.io.InputStream `抽象类是表示字节输入流的所有类的超类，可以读取字节信息到内存中。它定义了字节输入流的基本共性功能方法。

- `public void close()` ：关闭此输入流并释放与此流相关联的任何系统资源。    
- `public abstract int read()`： 从输入流读取数据的下一个字节。 
- `public int read(byte[] b)`： 从输入流中读取一些字节数，并将它们存储到字节数组 b中 。

> 小贴士：
>
> close方法，当完成流的操作时，必须调用此方法，释放系统资源。

### 3.2.1FileInputStream类

`java.io.FileInputStream `类是文件输入流，从文件中读取字节。

**构造方法**

* `FileInputStream(File file)`： 通过打开与实际文件的连接来创建一个 FileInputStream ，该文件由文件系统中的 File对象 file命名。 
* `FileInputStream(String name)`： 通过打开与实际文件的连接来创建一个 FileInputStream ，该文件由文件系统中的路径名 name命名。  

当你创建一个流对象时，必须传入一个文件路径。该路径下，如果没有该文件,会抛出`FileNotFoundException` 。

- 构造举例，代码如下：

```java
public class FileInputStreamConstructor throws IOException{
    public static void main(String[] args) {
   	 	// 使用File对象创建流对象
        File file = new File("a.txt");
        FileInputStream fos = new FileInputStream(file);
      
        // 使用文件名称创建流对象
        FileInputStream fos = new FileInputStream("b.txt");
    }
}
```

**读取字节数据**

1. **读取字节**：`read`方法，每次可以读取一个字节的数据，提升为int类型，读取到文件末尾，返回`-1`，代码使用演示：

```java
public class FISRead {
    public static void main(String[] args) throws IOException{
      	// 使用文件名称创建流对象
       	FileInputStream fis = new FileInputStream("read.txt");
      	// 读取数据，返回一个字节
        int read = fis.read();
        System.out.println((char) read);
        read = fis.read();
        System.out.println((char) read);
        read = fis.read();
        System.out.println((char) read);
        read = fis.read();
        System.out.println((char) read);
        read = fis.read();
        System.out.println((char) read);
      	// 读取到末尾,返回-1
       	read = fis.read();
        System.out.println( read);
		// 关闭资源
        fis.close();
    }
}
输出结果：
a
b
c
d
e
-1
```

循环改进读取方式，代码使用演示：

```java
public class FISRead {
    public static void main(String[] args) throws IOException{
      	// 使用文件名称创建流对象
       	FileInputStream fis = new FileInputStream("read.txt");
      	// 定义变量，保存数据
        int b ；
        // 循环读取
        while ((b = fis.read())!=-1) {
            System.out.println((char)b);
        }
		// 关闭资源
        fis.close();
    }
}
输出结果：
a
b
c
d
e
```

> 小贴士：
>
> 1. 虽然读取了一个字节，但是会自动提升为int类型。
> 2. 流操作完毕后，必须释放系统资源，调用close方法，千万记得。

2. **使用字节数组读取**：`read(byte[] b)`，每次读取b的长度个字节到数组中，返回读取到的有效字节个数，读取到末尾时，返回`-1` ，代码使用演示：

```java
public class FISRead {
    public static void main(String[] args) throws IOException{
      	// 使用文件名称创建流对象.
       	FileInputStream fis = new FileInputStream("read.txt"); // 文件中为abcde
      	// 定义变量，作为有效个数
        int len ；
        // 定义字节数组，作为装字节数据的容器   
        byte[] b = new byte[2];
        // 循环读取
        while (len= fis.read(b))!=-1) {
           	// 每次读取后,把数组变成字符串打印
            System.out.println(new String(b));
        }
		// 关闭资源
        fis.close();
    }
}

输出结果：
ab
cd
ed
```

错误数据`d`，是由于最后一次读取时，只读取一个字节`e`，数组中，上次读取的数据没有被完全替换，所以要通过`len` ，获取有效的字节，代码使用演示：

```java
public class FISRead {
    public static void main(String[] args) throws IOException{
      	// 使用文件名称创建流对象.
       	FileInputStream fis = new FileInputStream("read.txt"); // 文件中为abcde
      	// 定义变量，作为有效个数
        int len ；
        // 定义字节数组，作为装字节数据的容器   
        byte[] b = new byte[2];
        // 循环读取
        while (( len= fis.read(b))!=-1) {
           	// 每次读取后,把数组的有效字节部分，变成字符串打印
            System.out.println(new String(b，0，len));//  len 每次读取的有效字节个数
        }
		// 关闭资源
        fis.close();
    }
}

输出结果：
ab
cd
e
```

> 小贴士：
>
> 使用数组读取，每次读取多个字节，减少了系统间的IO操作次数，从而提高了读写的效率，建议开发中使用。

**字节流练习：图片复制**

复制原理图解：

![](IO流、序列化.assets/2_copy.jpg)

**案例实现**

复制图片文件，代码使用演示：

```java
public class Copy {
    public static void main(String[] args) throws IOException {
        // 1.创建流对象
        // 1.1 指定数据源
        FileInputStream fis = new FileInputStream("D:\\test.jpg");
        // 1.2 指定目的地
        FileOutputStream fos = new FileOutputStream("test_copy.jpg");

        // 2.读写数据
        // 2.1 定义数组
        byte[] b = new byte[1024];
        // 2.2 定义长度
        int len;
        // 2.3 循环读取
        while ((len = fis.read(b))!=-1) {
            // 2.4 写出数据
            fos.write(b, 0 , len);
        }

        // 3.关闭资源
        fos.close();
        fis.close();
    }
}
```

> 小贴士：
>
> 流的关闭原则：先开后关，后开先关。

## 3.3字符输入流【Reader】

`java.io.Reader`抽象类是表示用于读取字符流的所有类的超类，可以读取字符信息到内存中。它定义了字符输入流的基本共性功能方法。

- `public void close()` ：关闭此流并释放与此流相关联的任何系统资源。    
- `public int read()`： 从输入流读取一个字符。 
- `public int read(char[] cbuf)`： 从输入流中读取一些字符，并将它们存储到字符数组 cbuf中 。缓冲流,也叫高效流，是对4个基本的 FileXxx 流的增强，所以也是4个流，按照数据类型分类：
  字节缓冲流： BufferedInputStream ， BufferedOutputStream
  字符缓冲流： BufferedReader ， BufferedWriter
  缓冲流的基本原理，是在创建流对象时，会创建一个内置的默认大小的缓冲区数组，通过缓冲区读写，减少系统IO
  次数，从而提高读写的效率。

### 3.3.1 FileReader类  

`java.io.FileReader `类是读取字符文件的便利类。构造时使用系统默认的字符编码和默认字节缓冲区。

> 小贴士：
>
> 1. 字符编码：字节与字符的对应规则。Windows系统的中文编码默认是GBK编码表。
>
>    idea中UTF-8
>
> 2. 字节缓冲区：一个字节数组，用来临时存储字节数据。

**构造方法**

- `FileReader(File file)`： 创建一个新的 FileReader ，给定要读取的File对象。   
- `FileReader(String fileName)`： 创建一个新的 FileReader ，给定要读取的文件的名称。  

当你创建一个流对象时，必须传入一个文件路径。类似于FileInputStream 。

- 构造举例，代码如下：

```java
public class FileReaderConstructor throws IOException{
    public static void main(String[] args) {
   	 	// 使用File对象创建流对象
        File file = new File("a.txt");
        FileReader fr = new FileReader(file);
      
        // 使用文件名称创建流对象
        FileReader fr = new FileReader("b.txt");
    }
}
```

**读取字符数据**

1. **读取字符**：`read`方法，每次可以读取一个字符的数据，提升为int类型，读取到文件末尾，返回`-1`，循环读取，代码使用演示：

```java
public class FRRead {
    public static void main(String[] args) throws IOException {
      	// 使用文件名称创建流对象
       	FileReader fr = new FileReader("read.txt");
      	// 定义变量，保存数据
        int b ；
        // 循环读取
        while ((b = fr.read())!=-1) {
            System.out.println((char)b);
        }
		// 关闭资源
        fr.close();
    }
}
输出结果：
黑
马
程
序
员
```

> 小贴士：虽然读取了一个字符，但是会自动提升为int类型。

2. **使用字符数组读取**：`read(char[] cbuf)`，每次读取b的长度个字符到数组中，返回读取到的有效字符个数，读取到末尾时，返回`-1` ，代码使用演示：

```java
public class FRRead {
    public static void main(String[] args) throws IOException {
      	// 使用文件名称创建流对象
       	FileReader fr = new FileReader("read.txt");
      	// 定义变量，保存有效字符个数
        int len ；
        // 定义字符数组，作为装字符数据的容器
         char[] cbuf = new char[2];
        // 循环读取
        while ((len = fr.read(cbuf))!=-1) {
            System.out.println(new String(cbuf));
        }
		// 关闭资源
        fr.close();
    }
}
输出结果：
黑马
程序
员序
```

获取有效的字符改进，代码使用演示：

```java
public class FISRead {
    public static void main(String[] args) throws IOException {
      	// 使用文件名称创建流对象
       	FileReader fr = new FileReader("read.txt");
      	// 定义变量，保存有效字符个数
        int len ；
        // 定义字符数组，作为装字符数据的容器
        char[] cbuf = new char[2];
        // 循环读取
        while ((len = fr.read(cbuf))!=-1) {
            System.out.println(new String(cbuf,0,len));
        }
    	// 关闭资源
        fr.close();
    }
}

输出结果：
黑马
程序
员
```



## 3.4字符输出流【Writer】

`java.io.Writer `抽象类是表示用于写出字符流的所有类的超类，将指定的字符信息写出到目的地。它定义了字节输出流的基本共性功能方法。

- `void write(int c)` 写入单个字符。
- `void write(char[] cbuf) `写入字符数组。 
- `abstract  void write(char[] cbuf, int off, int len) `写入字符数组的某一部分,off数组的开始索引,len写的字符个数。 
- `void write(String str) `写入字符串。 
- `void write(String str, int off, int len)` 写入字符串的某一部分,off字符串的开始索引,len写的字符个数。
- `void flush() `刷新该流的缓冲。  
- `void close()` 关闭此流，但要先刷新它。 

### 3.4.1 FileWriter类

`java.io.FileWriter `类是写出字符到文件的便利类。构造时使用系统默认的字符编码和默认字节缓冲区。

**构造方法**

- `FileWriter(File file)`： 创建一个新的 FileWriter，给定要读取的File对象。   
- `FileWriter(String fileName)`： 创建一个新的 FileWriter，给定要读取的文件的名称。  

当你创建一个流对象时，必须传入一个文件路径，类似于FileOutputStream。

- 构造举例，代码如下：

```java
public class FileWriterConstructor {
    public static void main(String[] args) throws IOException {
   	 	// 使用File对象创建流对象
        File file = new File("a.txt");
        FileWriter fw = new FileWriter(file);
      
        // 使用文件名称创建流对象
        FileWriter fw = new FileWriter("b.txt");
    }
}
```

**基本写出数据**

**写出字符**：`write(int b)` 方法，每次可以写出一个字符数据，代码使用演示：

```java
public class FWWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileWriter fw = new FileWriter("fw.txt");     
      	// 写出数据
      	fw.write(97); // 写出第1个字符
      	fw.write('b'); // 写出第2个字符
      	fw.write('C'); // 写出第3个字符
      	fw.write(30000); // 写出第4个字符，中文编码表中30000对应一个汉字。
      
      	/*
        【注意】关闭资源时,与FileOutputStream不同。
      	 如果不关闭,数据只是保存到缓冲区，并未保存到文件。
        */
        // fw.close();
    }
}
输出结果：
abC田
```

> 小贴士：
>
> 1. 虽然参数为int类型四个字节，但是只会保留一个字符的信息写出。
> 2. 未调用close方法，数据只是保存到了缓冲区，并未写出到文件中。

**关闭和刷新**

因为内置缓冲区的原因，如果不关闭输出流，无法写出字符到文件中。但是关闭的流对象，是无法继续写出数据的。如果我们既想写出数据，又想继续使用流，就需要`flush` 方法了。

* `flush` ：刷新缓冲区，流对象可以继续使用。
* `close `:先刷新缓冲区，然后通知系统释放资源。流对象不可以再被使用了。

代码使用演示：

```java
public class FWWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileWriter fw = new FileWriter("fw.txt");
        // 写出数据，通过flush
        fw.write('刷'); // 写出第1个字符
        fw.flush();
        fw.write('新'); // 继续写出第2个字符，写出成功
        fw.flush();
      
      	// 写出数据，通过close
        fw.write('关'); // 写出第1个字符
        fw.close();
        fw.write('闭'); // 继续写出第2个字符,【报错】java.io.IOException: Stream closed
        fw.close();
    }
}
```

> 小贴士：即便是flush方法写出了数据，操作的最后还是要调用close方法，释放系统资源。

**写出其他数据**

1. **写出字符数组** ：`write(char[] cbuf)` 和 `write(char[] cbuf, int off, int len)` ，每次可以写出字符数组中的数据，用法类似FileOutputStream，代码使用演示：

```java
public class FWWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileWriter fw = new FileWriter("fw.txt");     
      	// 字符串转换为字节数组
      	char[] chars = "黑马程序员".toCharArray();
      
      	// 写出字符数组
      	fw.write(chars); // 黑马程序员
        
		// 写出从索引2开始，2个字节。索引2是'程'，两个字节，也就是'程序'。
        fw.write(b,2,2); // 程序
      
      	// 关闭资源
        fos.close();
    }
}
```

2. **写出字符串**：`write(String str)` 和 `write(String str, int off, int len)` ，每次可以写出字符串中的数据，更为方便，代码使用演示：

```java
public class FWWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象
        FileWriter fw = new FileWriter("fw.txt");     
      	// 字符串
      	String msg = "黑马程序员";
      
      	// 写出字符数组
      	fw.write(msg); //黑马程序员
      
		// 写出从索引2开始，2个字节。索引2是'程'，两个字节，也就是'程序'。
        fw.write(msg,2,2);	// 程序
      	
        // 关闭资源
        fos.close();
    }
}
```

3. **续写和换行**：操作类似于FileOutputStream。

```java
public class FWWrite {
    public static void main(String[] args) throws IOException {
        // 使用文件名称创建流对象，可以续写数据
        FileWriter fw = new FileWriter("fw.txt"，true);     
      	// 写出字符串
        fw.write("黑马");
      	// 写出换行
      	fw.write("\r\n");
      	// 写出字符串
  		fw.write("程序员");
      	// 关闭资源
        fw.close();
    }
}
输出结果:
黑马
程序员
```

> 小贴士：字符流，只能操作文本文件，不能操作图片，视频等非文本文件。
>
> 当我们单纯读或者写文本文件时  使用字符流 其他情况使用字节流

# 4.缓冲流

缓冲流,也叫高效流，是对4个基本的 FileXxx 流的增强，要“套接”在相应的节点流之上，根据数据操作单位可以把缓冲流分为：

- 字节缓冲流： BufferedInputStream ， BufferedOutputStream
- 字符缓冲流： BufferedReader ， BufferedWriter

缓冲流的基本原理，是在创建流对象时，会创建一个内置的默认大小的缓冲区数组，缺省使用**8192个字节(8Kb)的缓冲区**。通过缓冲区读写，减少系统IO次数，从而提高读写的效率。

![image-20200920193014713](IO%E6%B5%81%E3%80%81%E5%BA%8F%E5%88%97%E5%8C%96.assets/image-20200920193014713.png)

**缓冲流读写方法与基本的流是一致的**

> **注意事项：**
>
>  当读取数据时，数据按块读入缓冲区，其后的读操作则直接访问缓冲区
>
>  当使用BufferedInputStream读取字节文件时，**BufferedInputStream会一次性从文件中读取8192个(8Kb)，存在缓冲区中，直到缓冲区装满了，才重新从文件中读取下一个8192个字节数组。**
>
>  向流中写入字节时，不会直接写到文件，先写到缓冲区中直到缓冲区写满，BufferedOutputStream才会把缓冲区中的数据一次性写到文件里。**使用方法flush()可以强制将缓冲区的内容全部写入输出流**
>
>  关闭流的顺序和打开流的顺序相反。只要关闭最外层流即可，关闭最外层流也会相应关闭内层节点流
>
>  `flush()`方法的使用：手动将buffer中内容写入文件
>
>  如果是带缓冲区的流对象的`close()`方法，不但会关闭流，还会在关闭流之前刷新缓冲区，关闭后不能再写出

## 4.1字节缓冲流 

**构造方法：**

- `public BufferedInputStream(InputStream in)` ：创建一个 新的缓冲输入流。
- `public BufferedOutputStream(OutputStream out)` ： 创建一个新的缓冲输出流。

**使用：**测试效率（使用基本流与使用缓冲流的执行效率）

通过复制大文件（375MB），测试它的效率。

```java
1. 基本流，代码如下：
public class BufferedDemo {
    public static void main(String[] args) throws FileNotFoundException {
        // 记录开始时间
        long start = System.currentTimeMillis();
        // 创建流对象
        try (
            FileInputStream fis = new FileInputStream("jdk9.exe");
            FileOutputStream fos = new FileOutputStream("copy.exe")
        ){
        // 读写数据
            int b;
            while ((b = fis.read()) != ‐1) {
            	fos.write(b);
        	}
        } catch (IOException e) {
        	e.printStackTrace();
        }
        // 记录结束时间
        long end = System.currentTimeMillis();
        System.out.println("普通流复制时间:"+(end ‐ start)+" 毫秒");
    }
}
十几分钟过去了...

```

```java
2. 缓冲流，代码如下：
public class BufferedDemo {
    public static void main(String[] args) throws FileNotFoundException {
        // 记录开始时间
        long start = System.currentTimeMillis();
        // 创建流对象
        try (
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("jdk9.exe"));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("copy.exe"));
        ){
            // 读写数据
            int b;
            while ((b = bis.read()) != ‐1) {
            	bos.write(b);
            }
        } catch (IOException e) {
       	 	e.printStackTrace();
        }
        // 记录结束时间
        long end = System.currentTimeMillis();
        System.out.println("缓冲流复制时间:"+(end ‐ start)+" 毫秒");
    }
}
缓冲流复制时间:8016 毫秒

```

```java
3.使用数组的方式，代码如下：
public class BufferedDemo {
    public static void main(String[] args) throws FileNotFoundException {
        // 记录开始时间
        long start = System.currentTimeMillis();
        // 创建流对象
        try (
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("jdk9.exe"));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("copy.exe"));
        ){
            // 读写数据
            int len;
            byte[] bytes = new byte[8*1024];
            while ((len = bis.read(bytes)) != ‐1) {
            	bos.write(bytes, 0 , len);
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        // 记录结束时间
        long end = System.currentTimeMillis();
        System.out.println("缓冲流使用数组复制时间:"+(end ‐ start)+" 毫秒");
    }
}
缓冲流使用数组复制时间:666 毫秒
```

## 4.2字符缓冲流

**构造方法**

- `public BufferedReader(Reader in)` ：创建一个新的缓冲输入流。
- `public BufferedWriter(Writer out)` ： 创建一个新的缓冲输出流。

**特有方法**
字符缓冲流的基本方法与普通字符流调用方式一致，除此之外还有两个特有方法。

- `BufferedReader： public String readLine()` : 一次读取字符文本文件的一行字符
- `BufferedWriter： public void newLine()` : 写一行行分隔符,由系统属性定义符号。

readLine 方法演示，代码如下：

```java
public class BufferedReaderDemo {
    public static void main(String[] args) throws IOException {
        // 创建流对象
        BufferedReader br = new BufferedReader(new FileReader("in.txt"));
        // 定义字符串,保存读取的一行文字
        String line = null;
        // 循环读取,读取到最后返回null
        while ((line = br.readLine())!=null) {
            System.out.print(line);
            System.out.println("‐‐‐‐‐‐");
        }
        // 释放资源
        br.close();
    }
}
```

newLine 方法演示，代码如下：

```java
public class BufferedWriterDemo throws IOException {
    public static void main(String[] args) throws IOException {
        // 创建流对象
        BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
        // 写出数据
        bw.write("黑马");
        // 写出换行
        bw.newLine();
        bw.write("程序");
        bw.newLine();
        bw.write("员");
        bw.newLine();
        // 释放资源
        bw.close();
    }
}
输出效果:
黑马
程序
员
```



# 5.转换流

- 转换流提供了在**字节流和字符流之间的转换**

- Java API提供了两个转换流：
  - **`InputStreamReader`：将InputStream转换为Reader**
  - **`OutputStreamWriter`：将Writer转换为OutputStream**
- 字节流中的数据都是字符时，转成字符流操作更高效。
- 很多时候我们使用转换流来处理文件乱码问题。实现编码和解码的功能。

## 5.1字符编码和字符集



## 5.2InputStreamReader类

转换流 java.io.InputStreamReader ，是Reader的子类，是从字节流到字符流的桥梁。它读取字节，并使用指定的字符集将其解码为字符。它的字符集可以由名称指定，也可以接受平台的默认字符集。
**构造方法**

- `InputStreamReader(InputStream in)` : 创建一个使用默认字符集的字符流。
- `InputStreamReader(InputStream in, String charsetName)` : 创建一个指定字符集的字符流。

构造举例，代码如下：

```java
InputStreamReader isr = new InputStreamReader(new FileInputStream("in.txt"));
InputStreamReader isr2 = new InputStreamReader(new FileInputStream("in.txt") ,"GBK");
```

**指定编码读取**

```java
public class ReaderDemo2 {
    public static void main(String[] args) throws IOException {
        // 定义文件路径,文件为gbk编码
        String FileName ="E:\\file_gbk.txt";
        // 创建流对象,默认UTF8编码
        InputStreamReader isr = new InputStreamReader(new FileInputStream(FileName));
        // 创建流对象,指定GBK编码
        InputStreamReader isr2 = new InputStreamReader(new FileInputStream(FileName) ,
        "GBK");
        // 定义变量,保存字符
        int read;
        
        // 使用默认编码字符流读取,乱码
        while ((read = isr.read()) != ‐1) {
        	System.out.print((char)read); // ��Һ�
        }
        isr.close();
        
        // 使用指定编码字符流读取,正常解析
        while ((read = isr2.read()) != ‐1) {
        	System.out.print((char)read);// 大家好
        }
        isr2.close();
    }
}

```

## 5.3OutputStreamWriter类

- 转换流 java.io.OutputStreamWriter ，是Writer的子类，是从字符流到字节流的桥梁。使用指定的字符集将字符
  编码为字节。它的字符集可以由名称指定，也可以接受平台的默认字符集。

**构造方法**

- `OutputStreamWriter(OutputStream in)` : 创建一个使用默认字符集的字符流。
- `OutputStreamWriter(OutputStream in, String charsetName)` : 创建一个指定字符集的字符流。

构造举例，代码如下：

```java
OutputStreamWriter isr = new OutputStreamWriter(new FileOutputStream("out.txt"));
OutputStreamWriter isr2 = new OutputStreamWriter(new FileOutputStream("out.txt") ,"GBK");

```

指定编码写出

```java
public class OutputDemo {
    public static void main(String[] args) throws IOException {
        // 定义文件路径
        String FileName ="E:\\out.txt";
        // 创建流对象,默认UTF8编码
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(FileName));
        // 写出数据
        osw.write("你好"); // 保存为6个字节
        osw.close();
        // 定义文件路径
        String FileName2 ="E:\\out2.txt";
        // 创建流对象,指定GBK编码
        OutputStreamWriter osw2 = new OutputStreamWriter(new FileOutputStream(FileName2),
        "GBK");
        // 写出数据
        osw2.write("你好");// 保存为4个字节
        osw2.close();
    }
}
```



# 6.标准输入流、输出流



# 7.打印流

**实现将基本数据类型的数据格式转化为字符串输出**

打印流：PrintStream和PrintWriter

- 提供了一系列重载的print()和println()方法，用于多种数据类型的输出

- PrintStream和PrintWriter的输出不会抛出IOException异常

- PrintStream和PrintWriter有自动flush功能

- PrintStream 打印的所有字符都使用平台的默认字符编码转换为字节。

  在需要写入字符而不是写入字节的情况下，应该使用 PrintWriter 类。

- System.out返回的是PrintStream的实例

```java
PrintStream ps = null;
try {
        FileOutputStream fos = new FileOutputStream(new File("D:\\IO\\text.txt"));
        // 创建打印输出流,设置为自动刷新模式(写入换行符或字节 '\n' 时都会刷新输出缓冲区)
        ps = new PrintStream(fos, true);
        if (ps != null) {// 把标准输出流(控制台输出)改成文件
        	System.setOut(ps);
        }
        for (int i = 0; i <= 255; i++) { // 输出ASCII字符
            System.out.print((char) i);
            if (i % 50 == 0) { // 每50个数据一行
            	System.out.println(); // 换行
            }
        }
} catch (FileNotFoundException e) {
        e.printStackTrace();
} finally {
        if (ps != null) {
        	ps.close();
    	}
}

```



# 8.数据流



# 9.对象流

## 9.1概述

- ObjectInputStream和OjbectOutputSteam
- 用于**存储和读取基本数据类型数据或对象**的处理流。它的强大之处就是可以把Java中的对象写入到数据源中，也能把对象从数据源中还原回来。
- **序列化：用ObjectOutputStream类保存基本类型数据或对象的机制**
-  **反序列化：用ObjectInputStream类读取基本类型数据或对象的机制**
- **ObjectOutputStream和ObjectInputStream不能序列化static和transient修饰的成员变量**

## 9.2 ObjectOutputStream类

`java.io.ObjectOutputStream` 类，将Java对象的原始数据类型写出到文件,实现对象的持久存储。

**构造方法**

`public ObjectOutputStream(OutputStream out)` ： 创建一个指定OutputStream的ObjectOutputStream。
构造举例，代码如下：

```java
FileOutputStream fileOut = new FileOutputStream("employee.txt");
ObjectOutputStream out = new ObjectOutputStream(fileOut);
```

**序列化操作**

1.序列化介绍

- 对象序列化机制允许把内存中的Java对象转换成平台无关的二进制流，而允许把这种二进制流持久地保存在磁盘上，或通过网络将这种二进制流传输到另一个网络节点。当其它程序获取了这种二进制流，就可以恢复成原来的Java对象
- 序列化的好处在于可将任何实现了Serializable接口的对象转化为字节数据，使其在保存和传输时可被还原
- 序列化是 RMI（Remote Method Invoke – 远程方法调用）过程的参数和返回值都必须实现的机制，而 RMI 是JavaEE 的基础。因此序列化机制是JavaEE 平台的基础
- 如果需要让某个对象支持序列化机制，则必须让对象所属的类及其属性是可序列化的，为了让某个类是可序列化的，该类必须实现如下两个接口之一。否则，会抛出`NotSerializableException`异常
  - Serializable
  - Externalizable

- 凡是实现Serializable接口的类都有一个表示序列化版本标识符的静态变量：
                    `private static final long serialVersionUID;`
- serialVersionUID用来表明类的不同版本间的兼容性。简言之，其目的是以序列化对象进行版本控制，有关各版本反序列化时是否兼容。
- 如果类没有显示定义这个静态常量，它的值是Java运行时环境根据类的内部细节自动生成的。若类的实例变量做了修改，serialVersionUID 可能发生变化。故建议，显式声明。
- 简单来说，Java的序列化机制是通过在运行时判断类的serialVersionUID来验证版本一致性的。在进行反序列化时，JVM会把传来的字节流中的serialVersionUID与本地相应实体类的serialVersionUID进行比较，如果相同就认为是一致的，可以进行反序列化，否则就会出现序列化版本不一致的异常。(InvalidCastException)

> 一个对象要想序列化，必须满足两个条件:
>
> 1. 该类必须实现 java.io.Serializable 接口， Serializable 是一个标记接口，不实现此接口的类将不会使任
>    何状态序列化或反序列化，会抛出 NotSerializableException 。
> 2. 该类的所有属性必须是可序列化的。如果有一个属性不需要可序列化的，则该属性必须注明是瞬态的，使用transient 关键字修饰。

```java
public class Employee implements java.io.Serializable {
    public String name;
    public String address;
    public transient int age; // transient瞬态修饰成员,不会被序列化
    public void addressCheck() {
   	 	System.out.println("Address check : " + name + "‐‐" + address);
    }
}

```

2.写出对象方法
`public final void writeObject (Object obj)` : 将指定的对象写出。

**使用对象流序列化对象**

- 若某个类实现了 Serializable 接口，该类的对象就是可序列化的：
- 创建一个 ObjectOutputStream调用 ObjectOutputStream 对象的 writeObject(对象) 方法输出可序列化对象
- 注意写出一次，操作flush()一次

```java
public class SerializeDemo{
    public static void main(String [] args) {
        Employee e = new Employee();
        e.name ="zhangsan";
        e.address ="beiqinglu";
        e.age = 20;
        try {
            // 创建序列化流对象
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("employee.txt"));
            // 写出对象
            out.writeObject(e);
            // 释放资源
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved"); // 姓名，地址被序列化，年龄没有被序列
            化。
        } catch(IOException i) {
        	i.printStackTrace();
        }
    }
}
输出结果：
Serialized data is saved

```

## 9.3 ObjectInputStream类

ObjectInputStream反序列化流，将之前使用ObjectOutputStream序列化的原始数据恢复为对象。

**构造方法**

`public ObjectInputStream(InputStream in)` ： 创建一个指定InputStream的ObjectInputStream。

**反序列化操作1**

如果能找到一个对象的class文件，我们可以进行反序列化操作，调用 ObjectInputStream 读取对象的方法：
                 `public final Object readObject ()` : 读取一个对象。

```java
public class DeserializeDemo {
    public static void main(String [] args) {
        Employee e = null;
        try {
            // 创建反序列化流
            FileInputStream fileIn = new FileInputStream("employee.txt");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            // 读取一个对象
            e = (Employee) in.readObject();
            // 释放资源
            in.close();
            fileIn.close();
        }catch(IOException i) {
            // 捕获其他异常
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c) {
            // 捕获类找不到异常
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }
        // 无异常,直接打印输出
        System.out.println("Name: " + e.name); // zhangsan
        System.out.println("Address: " + e.address); // beiqinglu
        System.out.println("age: " + e.age); // 0
    }
}
```

对于JVM可以反序列化对象，它必须是能够找到class文件的类。如果找不到该类的class文件，则抛出一个
`ClassNotFoundException` 异常。

**反序列化操作2**

另外，当JVM反序列化对象时，能找到class文件，但是class文件在序列化对象之后发生了修改，那么反序列化操
作也会失败，抛出一个 InvalidClassException 异常。发生这个异常的原因如下：

- 该类的序列版本号与从流中读取的类描述符的版本号不匹配
- 该类包含未知数据类型
- 该类没有可访问的无参数构造方法

Serializable 接口给需要序列化的类，提供了一个序列版本号。 serialVersionUID 该版本号的目的在于验证序
列化的对象和对应类是否版本匹配。

```java
public class Employee implements java.io.Serializable {
    // 加入序列版本号
    private static final long serialVersionUID = 1L;
    public String name;
    public String address;
    // 添加新的属性 ,重新编译, 可以反序列化,该属性赋为默认值.
    public int eid;
    public void addressCheck() {
    	System.out.println("Address check : " + name + "‐‐" + address);
    }
}
```



# 10.随机存取文件流



# 11. NIO.2中Path、Paths、Files类的使用





