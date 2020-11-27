### LIMIT以及LIMIT OFFSET的区别

**1.LIMIT使用方法**

```mysql
LIMIT [参数1]--m，参数2--n；
#表示从跳过m条数据开始取n行数据
#参数1为可选参数，表示跳过m条数据（默认为0），eg:1表示从第二行开始
#参数2为必选参数，表示取几行数据
eg1：
SELECT * FROM table LIMIT 5;     //检索前 5 个记录行
等价于
SELECT * FROM table LIMIT 0,5;     //检索前 5 个记录行
eg2：
SELECT * FROM table LIMIT 5,10;  // 检索记录行 6-15
#为了检索某行开始到最后的所有数据，可以设置第二个参数为-1
eg3：
SELECT * FROM table LIMIT 95,-1; // 检索记录行 96-last
```

**2.LIMIT OFFSET使用方法**

```mysql
LIMIT 参数1--m OFFSET 参数2--n
#表示跳过n个数据，取m个数据
#参数1表示读取m条数据
#参数2表示跳过n个数据
eg4：
SELECT * FROM table LIMIT 2 OFFSET 1;  //跳过1条数据读取2条数据，即读取2-3条数据

```

**3.LIMINT 和 LIMIT OFFSET区别**

```mysql
直接看例子：

select * from table limit 2,1;
//跳过2条取出1条数据，limit后面是从第2条开始读，读取1条信息，即读取第3条数据


select * from table limit 2 offset 1;
//从第1条（不包括）数据开始取出2条数据，limit后面跟的是2条数据，offset后面是从第1条开始读取，即读取第2,3条


```

