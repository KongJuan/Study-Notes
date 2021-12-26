## 01.Redis 面试常见问答

### 1. 什么是缓存雪崩？怎么解决？

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBM7jJsZ3yPOTGg2Sm9u4ODD3zIbMtZSSN6pUia1KScHqp8aKsV8zakWic7Js7K35Qnia871EcM2asAA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

通常，我们会使用缓存用于缓冲对 DB 的冲击，如果缓存宕机，所有请求将直接打在 DB，造成 DB 宕机——从而导致整个系统宕机。

#### 如何解决呢？

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBM7jJsZ3yPOTGg2Sm9u4ODLv913ASCZ4ibCkpFohGlSEOuibAcKI3yPDbzdc5WgOophDYSuv6b5ibwg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**2 种策略（同时使用）：**

- 对缓存做高可用，防止缓存宕机
- 使用断路器，如果缓存宕机，为了防止系统全部宕机，限制部分流量进入 DB，保证部分可用，其余的请求返回断路器的默认值。

### 2. 什么是缓存穿透？怎么解决？

**解释 1：**缓存查询一个没有的 key，同时数据库也没有，如果黑客大量的使用这种方式，那么就会导致 DB 宕机。

**解决方案：**我们可以使用一个默认值来防止，例如，当访问一个不存在的 key，然后再去访问数据库，还是没有，那么就在缓存里放一个占位符，下次来的时候，检查这个占位符，如果发生时占位符，就不去数据库查询了，防止 DB 宕机。

**解释 2：**大量请求查询一个刚刚失效的 key，导致 DB 压力倍增，可能导致宕机，但实际上，查询的都是相同的数据。

**解决方案：**可以在这些请求代码加上双重检查锁。但是那个阶段的请求会变慢。不过总比 DB 宕机好。

### 3. 什么是缓存并发竞争？怎么解决？

**解释：**多个客户端写一个 key，如果顺序错了，数据就不对了。但是顺序我们无法控制。

**解决方案：**使用分布式锁，例如 zk，同时加入数据的时间戳。同一时刻，只有抢到锁的客户端才能写入，同时，写入时，比较当前数据的时间戳和缓存中数据的时间戳。

### 4.什么是缓存和数据库双写不一致？怎么解决？

解释：连续写数据库和缓存，但是操作期间，出现并发了，数据不一致了。

通常，更新缓存和数据库有以下几种顺序：

- 先更新数据库，再更新缓存。
- 先删缓存，再更新数据库。
- 先更新数据库，再删除缓存。

*三种方式的优劣来看一下：*

**先更新数据库，再更新缓存。**

这么做的问题是：当有 2 个请求同时更新数据，那么如果不使用分布式锁，将无法控制最后缓存的值到底是多少。也就是并发写的时候有问题。

**先删缓存，再更新数据库。**

这么做的问题：如果在删除缓存后，有客户端读数据，将可能读到旧数据，并有可能设置到缓存中，导致缓存中的数据一直是老数据。

有 2 种解决方案：

- 使用“双删”，即删更删，最后一步的删除作为异步操作，就是防止有客户端读取的时候设置了旧值。
- 使用队列，当这个 key 不存在时，将其放入队列，串行执行，必须等到更新数据库完毕才能读取数据。

总的来讲，比较麻烦。

**先更新数据库，再删除缓存**

这个实际是常用的方案，但是有很多人不知道，这里介绍一下，这个叫 Cache Aside Pattern，老外发明的。如果先更新数据库，再删除缓存，那么就会出现更新数据库之前有瞬间数据不是很及时。

同时，如果在更新之前，缓存刚好失效了，读客户端有可能读到旧值，然后在写客户端删除结束后再次设置了旧值，非常巧合的情况。

有 2 个前提条件：**缓存在写之前的时候失效，同时，在写客户度删除操作结束后，放置旧数据 —— 也就是读比写慢。****设置有的写操作还会锁表。**

所以，这个很难出现，但是如果出现了怎么办？使用双删！！！记录更新期间有没有客户端读数据库，如果有，在更新完数据库之后，执行延迟删除。

还有一种可能，如果执行更新数据库，准备执行删除缓存时，服务挂了，执行删除失败怎么办？？？

这就坑了！！！不过可以通过订阅数据库的 binlog 来删除。

### 参考

> https://coolshell.cn/articles/17416.html
> https://www.cnblogs.com/rjzheng/p/9041659.html
> https://docs.microsoft.com/en-us/azure/architecture/patterns/cache-aside

## 02.内存耗尽后Redis会发生什么

### 前言

作为一台服务器来说，内存并不是无限的，所以总会存在内存耗尽的情况，那么当 `Redis` 服务器的内存耗尽后，如果继续执行请求命令，`Redis` 会如何处理呢？

### 内存回收

使用`Redis` 服务时，很多情况下某些键值对只会在特定的时间内有效，为了防止这种类型的数据一直占有内存，我们可以**给键值对设置有效期**。`Redis` 中可以通过 `4` 个独立的命令来给一个键设置过期时间：

- `expire key ttl`：将 `key` 值的过期时间设置为 `ttl` **秒**。
- `pexpire key ttl`：将 `key` 值的过期时间设置为 `ttl` **毫秒**。
- `expireat key timestamp`：将 `key` 值的过期时间设置为指定的 `timestamp` **秒数**。
- `pexpireat key timestamp`：将 `key` 值的过期时间设置为指定的 `timestamp` **毫秒数**。

PS：**不管使用哪一个命令，最终 `Redis` 底层都是使用 `pexpireat` 命令来实现的**。另外，`set` 等命令也可以设置 `key` 的同时加上过期时间，这样可以保证设值和设过期时间的原子性。

设置了有效期后，可以通过 `ttl` 和 `pttl` 两个命令来查询剩余过期时间（如果未设置过期时间则下面两个命令返回 `-1`，如果设置了一个非法的过期时间，则都返回 `-2`）：

- `ttl key` 返回 `key` 剩余过期秒数。
- `pttl key` 返回 `key` 剩余过期的毫秒数。

#### 过期策略

如果**将一个过期的键删除**，我们一般都会有三种策略：

- 定时删除：**为每个键设置一个定时器，一旦过期时间到了，则将键删除**。这种策略对内存很友好，但是对 `CPU` 不友好，因为每个定时器都会占用一定的 `CPU` 资源。
- 惰性删除：**不管键有没有过期都不主动删除，等到每次去获取键时再判断是否过期，如果过期就删除该键，否则返回键对应的值**。这种策略对内存不够友好，可能会浪费很多内存。
- 定期扫描：**系统每隔一段时间就定期扫描一次，发现过期的键就进行删除**。这种策略相对来说是上面两种策略的折中方案，需要注意的是这个定期的频率要结合实际情况掌控好，使用这种方案有一个缺陷就是可能会出现已经过期的键也被返回。

在 `Redis` 当中，其选择的是策略 `2` 和策略 `3` 的综合使用。不过 `Redis` 的定期扫描只会扫描设置了过期时间的键，因为设置了过期时间的键 `Redis` 会单独存储，所以不会出现扫描所有键的情况：

```C
typedef struct redisDb {
   dict *dict; //所有的键值对
   dict *expires; //设置了过期时间的键值对
   dict *blocking_keys; //被阻塞的key,如客户端执行BLPOP等阻塞指令时
   dict *watched_keys; //WATCHED keys
   int id; //Database ID
   //... 省略了其他属性
} redisDb;
```

#### 8 种淘汰策略

假如 `Redis` 当中所有的键都没有过期，而且此时内存满了，那么客户端继续执行 `set` 等命令时 `Redis` 会怎么处理呢？`Redis` 当中提供了不同的淘汰策略来处理这种场景。

首先 `Redis` 提供了一个参数 `maxmemory` 来配置 `Redis` 最大使用内存：

```
maxmemory <bytes>
```

或者也可以通过命令 `config set maxmemory 1GB` 来动态修改。

如果没有设置该参数，那么在 `32` 位的操作系统中 `Redis` 最多使用 `3GB` 内存，而在 `64` 位的操作系统中则不作限制。

`Redis` 中提供了 `8` 种淘汰策略，可以通过参数 `maxmemory-policy` 进行配置：

PS：淘汰策略也可以直接使用命令 `config set maxmemory-policy <策略>` 来进行动态配置。

#### LRU 算法

`LRU` 全称为：`Least Recently Used`。即：最近最长时间未被使用。这个**主要针对的是使用时间**。

##### Redis 改进后的 LRU 算法

在 `Redis` 当中，并没有采用传统的 `LRU` 算法，因为**传统的 `LRU` 算法存在 `2` 个问题：**

- 需要额外的空间进行存储。
- 可能存在某些 `key` 值使用很频繁，但是最近没被使用，从而被 `LRU` 算法删除。

为了避免以上 `2` 个问题，`Redis` 当中对传统的 `LRU` 算法进行了改造，**通过抽样的方式进行删除**。

配置文件中提供了一个属性 `maxmemory_samples 5`，默认值就是 `5`，表示随机抽取 `5` 个 `key` 值，然后对这 `5` 个 `key` 值按照 `LRU` 算法进行删除，所以很明显，`key` 值越大，删除的准确度越高。

对抽样 `LRU` 算法和传统的 `LRU` 算法，`Redis` 官网当中有一个对比图：

- 浅灰色带是被删除的对象。
- 灰色带是未被删除的对象。
- 绿色是添加的对象。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XCQmMta4rXrSq5tMhfibdMCgIoXLBXyJqPN8Cfxjjy2hUYJcEL9EkMZsk90uL34KmQGMBzXTLibvMsQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

左上角第一幅图代表的是传统 `LRU` 算法，可以看到，当抽样数达到 `10` 个（右上角），已经和传统的 `LRU` 算法非常接近了。

##### Redis 如何管理热度数据

前面我们讲述字符串对象时，提到了 `redisObject` 对象中存在一个 `lru` 属性：

```c
typedef struct redisObject {
    unsigned type:4;//对象类型（4位=0.5字节）
    unsigned encoding:4;//编码（4位=0.5字节）
    unsigned lru:LRU_BITS;//记录对象最后一次被应用程序访问的时间（24位=3字节）
    int refcount;//引用计数。等于0时表示可以被垃圾回收（32位=4字节）
    void *ptr;//指向底层实际的数据存储结构，如：SDS等(8字节)
} robj;
```

**`lru` 属性是创建对象的时候写入，对象被访问到时也会进行更新**。正常人的思路就是最后决定要不要删除某一个键肯定是用当前时间戳减去 `lru`，差值最大的就优先被删除。但是 `Redis` 里面并不是这么做的，**`Redis` 中维护了一个全局属性 `lru_clock`，这个属性是通过一个全局函数 `serverCron` 每隔 `100` 毫秒执行一次来更新的，记录的是当前 `unix` 时间戳**。

**最后决定删除的数据是通过 `lru_clock` 减去对象的 `lru` 属性而得出的**。那么为什么 `Redis` 要这么做呢？直接取全局时间不是更准确吗？

这是因为这么做可以避免每次更新对象的 `lru` 属性的时候可以直接取全局属性，而不需要去调用系统函数来获取系统时间，从而提升效率（`Redis` 当中有很多这种细节考虑来提升性能，可以说是对性能尽可能的优化到极致）。

不过这里还有一个问题，我们看到，`redisObject` 对象中的 `lru` 属性只有 `24` 位，`24` 位只能存储 `194` 天的时间戳大小，一旦超过 `194` 天之后就会重新从 `0` 开始计算，所以这时候就可能会出现 `redisObject` 对象中的 `lru` 属性大于全局的 `lru_clock` 属性的情况。

正因为如此，所以计算的时候也需要分为 `2` 种情况：

- 当全局 `lruclock` > `lru`，则使用 `lruclock` - `lru` 得到空闲时间。
- 当全局 `lruclock` < `lru`，则使用 `lruclock_max`（即 `194` 天） - `lru` + `lruclock` 得到空闲时间。

需要注意的是，这种计算方式并不能保证抽样的数据中一定能删除空闲时间最长的。这是因为首先超过 `194` 天还不被使用的情况很少，再次只有 `lruclock` 第 `2` 轮继续超过 `lru` 属性时，计算才会出问题。

比如对象 `A` 记录的 `lru` 是 `1` 天，而 `lruclock` 第二轮都到 `10` 天了，这时候就会导致计算结果只有 `10-1=9` 天，实际上应该是 `194+10-1=203` 天。但是这种情况可以说又是更少发生，所以说这种处理方式是可能存在删除不准确的情况，但是本身这种算法就是一种近似的算法，所以并不会有太大影响。

#### LFU 算法

`LFU` 全称为：`Least Frequently Used`。即：最近最少频率使用，这个**主要针对的是使用频率**。这个属性也是记录在`redisObject` 中的 `lru` 属性内。

**当我们采用 `LFU` 回收策略时，`lru` 属性的高 `16` 位用来记录访问时间（last decrement time：ldt，单位为分钟），低 `8` 位用来记录访问频率（logistic counter：logc），简称 `counter`。**

##### 访问频次递增

`LFU` 计数器每个键只有 `8` 位，它能表示的最大值是 `255`，所以 `Redis` 使用的是一种基于**概率的对数器**来实现 `counter` 的递增。

给定一个旧的访问频次，当一个键被访问时，`counter` 按以下方式递增：

1. 提取 `0` 和 `1` 之间的随机数 `R`。
2. `counter` - 初始值（默认为 `5`），得到一个基础差值，如果这个差值小于 `0`，则直接取 `0`，为了方便计算，把这个差值记为 `baseval`。
3. 概率 `P` 计算公式为：`1/(baseval * lfu_log_factor + 1)`。
4. 如果 `R < P` 时，频次进行递增（`counter++`）。

公式中的 `lfu_log_factor` 称之为对数因子，默认是 `10` ，可以通过参数来进行控制：

```
lfu_log_factor 10
```

下图就是对数因子 `lfu_log_factor` 和频次 `counter` 增长的关系图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XCQmMta4rXrSq5tMhfibdMCg92CNhj6yPicxicAXkafVTjEXz4fibkpia2K8NIzX7xBR8S3lOFpD0q9jZw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到，当对数因子 `lfu_log_factor` 为 `100` 时，大概是 `10M（1000万）` 次访问才会将访问 `counter` 增长到 `255`，而默认的 `10` 也能支持到 `1M（100万）` 次访问 `counter` 才能达到 `255` 上限，这在大部分场景都是足够满足需求的。

##### 访问频次递减

如果访问频次 `counter` 只是一直在递增，那么迟早会全部都到 `255`，也就是说 `counter` 一直递增不能完全反应一个 `key` 的热度的，所以当某一个 `key` 一段时间不被访问之后，`counter` 也需要对应减少。

`counter` 的减少速度由参数 `lfu-decay-time` 进行控制，默认是 `1`，单位是分钟。默认值 `1` 表示：`N` 分钟内没有访问，`counter` 就要减 `N`。

```
lfu-decay-time 1
```

具体算法如下：

1. 获取当前时间戳，转化为**分钟**后取低 `16` 位（为了方便后续计算，这个值记为 `now`）。
2. 取出对象内的 `lru` 属性中的高 `16` 位（为了方便后续计算，这个值记为 `ldt`）。
3. 当 `lru` > `now` 时，默认为过了一个周期（`16` 位，最大 `65535`)，则取差值 `65535-ldt+now`：当 `lru` <= `now` 时，取差值 `now-ldt`（为了方便后续计算，这个差值记为 `idle_time`）。
4. 取出配置文件中的 `lfu_decay_time` 值，然后计算：`idle_time / lfu_decay_time`（为了方便后续计算，这个值记为`num_periods`）。
5. 最后将`counter`减少：`counter - num_periods`。

看起来这么复杂，其实计算公式就是一句话：取出当前的时间戳和对象中的 `lru` 属性进行对比，计算出当前多久没有被访问到，比如计算得到的结果是 `100` 分钟没有被访问，然后再去除配置参数 `lfu_decay_time`，如果这个配置默认为 `1`也即是 `100/1=100`，代表 `100` 分钟没访问，所以 `counter` 就减少 `100`。

### 总结

本文主要介绍了 `Redis` 过期键的处理策略，以及当服务器内存不够时 `Redis` 的 `8` 种淘汰策略，最后介绍了 `Redis` 中的两种主要的淘汰算法 `LRU` 和 `LFU`。

## 03.Redis 内存满了怎么办

### Redis占用内存大小

我们知道Redis是基于内存的key-value数据库，因为系统的内存大小有限，所以我们在使用Redis的时候可以配置Redis能使用的最大的内存大小。

#### 1、通过配置文件配置

通过在Redis安装目录下面的redis.conf配置文件中添加以下配置设置内存大小

```
//设置Redis最大占用内存大小为100M
maxmemory 100mb
```

redis的配置文件不一定使用的是安装目录下面的redis.conf文件，启动redis服务的时候是可以传一个参数指定redis的配置文件的

#### 2、通过命令修改

Redis支持运行时通过命令动态修改内存大小

```
//设置Redis最大占用内存大小为100M
127.0.0.1:6379> config set maxmemory 100mb
//获取设置的Redis能使用的最大内存大小
127.0.0.1:6379> config get maxmemory
```

**如果不设置最大内存大小或者设置最大内存大小为0，在64位操作系统下不限制内存大小，在32位操作系统下最多使用3GB内存**

### Redis的内存淘汰

既然可以设置Redis最大占用内存大小，那么配置的内存就有用完的时候。那在内存用完的时候，还继续往Redis里面添加数据不就没内存可用了吗？

实际上Redis定义了几种策略用来处理这种情况：

- noeviction(默认策略)：对于写请求不再提供服务，直接返回错误（DEL请求和部分特殊请求除外）
- allkeys-lru：从所有key中使用LRU算法进行淘汰
- volatile-lru：从设置了过期时间的key中使用LRU算法进行淘汰
- allkeys-random：从所有key中随机淘汰数据
- volatile-random：从设置了过期时间的key中随机淘汰
- volatile-ttl：在设置了过期时间的key中，根据key的过期时间进行淘汰，越早过期的越优先被淘汰

当使用volatile-lru、volatile-random、volatile-ttl这三种策略时，如果没有key可以被淘汰，则和noeviction一样返回错误

#### 如何获取及设置内存淘汰策略

获取当前内存淘汰策略：

```
127.0.0.1:6379> config get maxmemory-policy
```

通过配置文件设置淘汰策略（修改redis.conf文件）：

```
maxmemory-policy allkeys-lru
```

通过命令修改淘汰策略：

```
127.0.0.1:6379> config set maxmemory-policy allkeys-lru
```

### LRU算法

#### 什么是LRU?

上面说到了Redis可使用最大内存使用完了，是可以使用LRU算法进行内存淘汰的，那么什么是LRU算法呢？

LRU(Least Recently Used)，即最近最少使用，是一种缓存置换算法。在使用内存作为缓存的时候，缓存的大小一般是固定的。当缓存被占满，这个时候继续往缓存里面添加数据，就需要淘汰一部分老的数据，释放内存空间用来存储新的数据。

这个时候就可以使用LRU算法了。其核心思想是：**如果一个数据在最近一段时间没有被用到，那么将来被使用到的可能性也很小，所以就可以被淘汰掉。**

使用java实现一个简单的LRU算法

```java
public class LRUCache<k, v> {
    //容量
    private int capacity;
    //当前有多少节点的统计
    private int count;
    //缓存节点
    private Map<k, Node<k, v>> nodeMap;
    private Node<k, v> head;
    private Node<k, v> tail;

    public LRUCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(String.valueOf(capacity));
        }
        this.capacity = capacity;
        this.nodeMap = new HashMap<>();
        //初始化头节点和尾节点，利用哨兵模式减少判断头结点和尾节点为空的代码
        Node headNode = new Node(null, null);
        Node tailNode = new Node(null, null);
        headNode.next = tailNode;
        tailNode.pre = headNode;
        this.head = headNode;
        this.tail = tailNode;
    }

    public void put(k key, v value) {
        Node<k, v> node = nodeMap.get(key);
        if (node == null) {
            if (count >= capacity) {
                //先移除一个节点
                removeNode();
            }
            node = new Node<>(key, value);
            //添加节点
            addNode(node);
        } else {
            //移动节点到头节点
            moveNodeToHead(node);
        }
    }

    public Node<k, v> get(k key) {
        Node<k, v> node = nodeMap.get(key);
        if (node != null) {
            moveNodeToHead(node);
        }
        return node;
    }

    private void removeNode() {
        Node node = tail.pre;
        //从链表里面移除
        removeFromList(node);
        nodeMap.remove(node.key);
        count--;
    }

    private void removeFromList(Node<k, v> node) {
        Node pre = node.pre;
        Node next = node.next;

        pre.next = next;
        next.pre = pre;

        node.next = null;
        node.pre = null;
    }

    private void addNode(Node<k, v> node) {
        //添加节点到头部
        addToHead(node);
        nodeMap.put(node.key, node);
        count++;
    }

    private void addToHead(Node<k, v> node) {
        Node next = head.next;
        next.pre = node;
        node.next = next;
        node.pre = head;
        head.next = node;
    }

    public void moveNodeToHead(Node<k, v> node) {
        //从链表里面移除
        removeFromList(node);
        //添加节点到头部
        addToHead(node);
    }

    class Node<k, v> {
        k key;
        v value;
        Node pre;
        Node next;

        public Node(k key, v value) {
            this.key = key;
            this.value = value;
        }
    }
}
```

上面这段代码实现了一个简单的LUR算法，代码很简单，也加了注释，仔细看一下很容易就看懂。

#### LRU在Redis中的实现

##### 近似LRU算法

Redis使用的是近似LRU算法，它跟常规的LRU算法还不太一样。近似LRU算法通过随机采样法淘汰数据，每次随机出5（默认）个key，从里面淘汰掉最近最少使用的key。

可以通过maxmemory-samples参数修改采样数量：

例：`maxmemory-samples 10`

**maxmenory-samples配置的越大，淘汰的结果越接近于严格的LRU算法**

Redis为了实现近似LRU算法，给每个key增加了一个额外增加了一个24bit的字段，用来存储该key最后一次被访问的时间。

##### Redis3.0对近似LRU的优化

Redis3.0对近似LRU算法进行了一些优化。**新算法会维护一个候选池（大小为16），池中的数据根据访问时间进行排序，第一次随机选取的key都会放入池中，随后每次随机选取的key只有在访问时间小于池中最小的时间才会放入池中，直到候选池被放满。当放满后，如果有新的key需要放入，则将池中最后访问时间最大（最近被访问）的移除。**

当需要淘汰的时候，则直接从池中选取最近访问时间最小（最久没被访问）的key淘汰掉就行。

##### LRU算法的对比

我们可以通过一个实验对比各LRU算法的准确率，先往Redis里面添加一定数量的数据n，使Redis可用内存用完，再往Redis里面添加n/2的新数据，这个时候就需要淘汰掉一部分的数据，如果按照严格的LRU算法，应该淘汰掉的是最先加入的n/2的数据。

生成如下各LRU算法的对比图

![图片](https://mmbiz.qpic.cn/mmbiz/eQPyBffYbucPAdbnicTBgibPrVOZ5CWHvRCBj1SWZuntX59vqxzupF4sic9foFujdWIIR28AibDaSu5ZZvv9LUsH3g/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

图片来源：segmentfault.com/a/1190000017555834

你可以看到图中有三种不同颜色的点：

- 浅灰色是被淘汰的数据
- 灰色是没有被淘汰掉的老数据
- 绿色是新加入的数据

我们能看到Redis3.0采样数是10生成的图最接近于严格的LRU。而同样使用5个采样数，Redis3.0也要优于Redis2.8。

### LFU算法

**LFU算法是Redis4.0里面新加的一种淘汰策略**。它的全称是Least Frequently Used，它的核心思想是**根据key的最近被访问的频率进行淘汰，很少被访问的优先被淘汰，被访问的多的则被留下来。**

LFU算法能更好的表示一个key被访问的热度。假如你使用的是LRU算法，一个key很久没有被访问到，只刚刚是偶尔被访问了一次，那么它就被认为是热点数据，不会被淘汰，而有些key将来是很有可能被访问到的则被淘汰了。如果使用LFU算法则不会出现这种情况，因为使用一次并不会使一个key成为热点数据。

LFU一共有两种策略：

- volatile-lfu：在设置了过期时间的key中使用LFU算法淘汰key
- allkeys-lfu：在所有的key中使用LFU算法淘汰数据

设置使用这两种淘汰策略跟前面讲的一样，不过要注意的一点是这两周策略只能在Redis4.0及以上设置，如果在Redis4.0以下设置会报错

### 问题

最后留一个小问题，可能有的人注意到了，我在文中并没有解释为什么Redis使用近似LRU算法而不使用准确的LRU算法，可以在评论区给出你的答案，大家一起讨论学习。

## 04.项目中如何对Redis内存进行优化

### 一、reids 内存分析

redis内存使用情况：info memory

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDCsaBQpGiaTXSZTVEkU2jPvE1sjt1ZnzcwoI7bs7nRibOd55rIn5oYnmQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

示例：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDNCc0AvAdVGEmlTQgwq7ImYwjAAXWRXAselRTbq4WGicTCIEnjwVibG9A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到，当前节点内存碎片率为`226893824/209522728≈1.08`，使用的内存分配器是jemalloc。

`used_memory_rss` 通常情况下是大于 `used_memory` 的，因为内存碎片的存在。

但是当操作系统把redis内存swap到硬盘时，memory_fragmentation_ratio 会小于1。redis使用硬盘作为内存，因为硬盘的速度，redis性能会受到极大的影响。

### 二、redis 内存使用

之前的文章 关于redis，你需要了解的几点！中我们简单介绍过redis的内存使用分布：自身内存，键值对象占用、缓冲区内存占用及内存碎片占用。

> https://www.cnblogs.com/niejunlei/p/12896605.html

redis 空进程自身消耗非常的少，可以忽略不计，优化内存可以不考虑此处的因素。

#### 1、对象内存

对象内存，也即真实存储的数据所占用的内存。

redis k-v结构存储，对象占用可以简单的理解为 `k-size + v-size`。

redis的键统一都为字符串类型，值包含多种类型：string、list、hash、set、zset五种基本类型及基于string的Bitmaps和HyperLogLog类型等。

在实际的应用中，一定要做好kv的构建形式及内存使用预期，可以参考 关于redis，你需要了解的几点！中关于不同值类型不同形式下的内部存储实现介绍。

#### 2、缓冲内存

缓冲内存包括三部分：客户端缓存、复制积压缓存及AOF缓冲区。

##### 1）客户端缓存

接入redis服务器的TCP连接输入输出缓冲内存占用，TCP输入缓冲占用是不受控制的，最大允许空间为1G。输出缓冲占用可以通过client-output-buffer-limit参数配置。

**redis 客户端主要分为从客户端、订阅客户端和普通客户端。**

**从客户端连接占用：**也就是我们所说的slave，主节点会为每一个从节点建立一条连接用于命令复制，缓冲配置为：`client-output-buffer-limit slave 256mb 64mb 60`。

主从之间的间络延迟及挂载的从节点数量是影响内存占用的主要因素。因此在涉及需要异地部署主从时要特别注意，另外，也要避免主节点上挂载过多的从节点（<=2）；

**订阅客户端内存占用：**发布订阅功能连接客户端使用单独的缓冲区，默认配置：client-output-buffer-limit pubsub 32mb 8mb 60。

当消费慢于生产时会造成缓冲区积压，因此需要特别注意消费者角色配比及生产、消费速度的监控。

普通客户端内存占用：除了上述之外的其它客户端，如我们通常的应用连接，默认配置：client-output-buffer-limit normal 1000。

可以看到，普通客户端没有配置缓冲区限制，通常一般的客户端内存消耗也可以忽略不计。

但是当redis服务器响应较慢时，容易造成大量的慢连接，主要表现为连接数的突增，如果不能及时处理，此时会严重影响redis服务节点的服务及恢复。

关于此，在实际应用中需要注意几点：

- maxclients最大连接数配置必不可少。
- 合理预估单次操作数据量（写或读）及网络时延ttl。
- 禁止线上大吞吐量命令操作，如keys等。

高并发应用情景下，redis内存使用需要有实时的监控预警机制，

##### 2）复制积压缓冲区

v2.8之后提供的一个可重用的固定大小缓冲区，用以实现向从节点的部分复制功能，避免全量复制。配置单数：`repl-backlog-size`，默认1M。单个主节点配置一个复制积压缓冲区。

##### 3）AOF缓冲区

AOF重写期间增量的写入命令保存，此部分缓存占用大小取决于AOF重写时间及增量。

#### 3、内存碎片内存占用

关于redis，你需要了解的几点！简单介绍过redis的内存分配方式。（更多面试题，欢迎关注公众号 Java面试题精选）

### 三、redis 子进程内存消耗

子进程即redis执行持久化（RDB/AOF）时fork的子任务进程。

#### 1、关于linux系统的写时复制机制：

父子进程会共享相同的物理内存页，父进程处理写请求时会对需要修改的页复制一份副本进行修改，子进程读取的内存则为fork时的父进程内存快照，因此，子进程的内存消耗由期间的写操作增量决定。

#### 2、关于linux的透明大页机制THP（Transparent Huge Page）：

THP机制会降低fork子进程的速度；写时复制内存页由4KB增大至2M。高并发情境下，写时复制内存占用消耗影响会很大，因此需要选择性关闭。

#### 3、关于linux配置：

一般需要配置linux系统 `vm.overcommit_memory=1`，以允许系统可以分配所有的物理内存。防止fork任务因内存而失败。

### 四、redis 内存管理

redis的内存管理主要分为两方面：内存上限控制及内存回收管理。

#### 1、内存上限：maxmemory

目的：缓存应用内存回收机制触发 + 防止物理内存用尽（redis 默认无限使用服务器内存） + 服务节点内存隔离（单服务器上部署多个redis服务节点）

在进行内存分配及限制时要充分考虑内存碎片占用影响。

动态调整，扩展redis服务节点可用内存：`config set maxmemory {}`。

#### 2、内存回收

回收时机：键过期、内存占用达到上限

##### 1）过期键删除：

redis 键过期时间保存在内部的过期字典中，redis采用惰性删除机制+定时任务删除机制。

**惰性删除：**即读时删除，读取带有超时属性的键时，如果键已过期，则删除然后返回空值。这种方式存在问题是，触发时机，加入过期键长时间未被读取，那么它将会一直存在内存中，造成内存泄漏。

**定时任务删除：**redis内部维护了一个定时任务（默认每秒10次，可配置），通过自适应法进行删除。

删除逻辑如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDt7Vh84hwzzb91leMxlm0YGQiawlibhDzaiayb6Ytl4R9EFkU3CticSMy3Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 需要说明的一点是，快慢模式执行的删除逻辑相同，这是超时时间不同。

##### 2）内存溢出控制

当内存达到maxmemory，会触发内存回收策略，具体策略依据maxmemory-policy来执行。

- noevication：默认不回收，达到内存上限，则不再接受写操作，并返回错误。
- volatile-lru：根据LRU算法删除设置了过期时间的键，如果没有则不执行回收。
- allkeys-lru：根据LRU算法删除键，针对所有键。
- allkeys-random：随机删除键。
- volatitle-random：速记删除设置了过期时间的键。
- volatilte-ttl：根据键ttl，删除最近过期的键，同样如果没有设置过期的键，则不执行删除。

动态配置：`config set maxmemory-policy {}`

在设置了maxmemory情况下，每次的redis操作都会检查执行内存回收，因此对于线上环境，要确保所这只的`maxmemory>used_memory`。

另外，可以通过动态配置maxmemory来主动触发内存回收。

## 05.遇到 Redis 线上连接超时一般如何处理

一封报警邮件，大量服务节点 redis 响应超时。

又来，好烦。

redis 响应变慢，查看日志，发现大量 TimeoutException。

大量TimeoutException，说明当前redis服务节点上已经堆积了大量的连接查询，超出redis服务能力，再次尝试连接的客户端，redis 服务节点直接拒绝，抛出错误。

**那到底是什么导致了这种情况的发生呢？**

总结起来，我们可以从以下几方面进行关注：

### 一、redis 服务节点受到外部关联影响

redis服务所在服务器，物理机的资源竞争及网络状况等。同一台服务器上的服务必然面对着服务资源的竞争，CPU，内存，固存等。

#### 1、CPU资源竞争

redis属于CPU密集型服务，对CPU资源依赖尤为紧密，当所在服务器存在其它CPU密集型应用时，必然会影响redis的服务能力，尤其是在其它服务对CPU资源消耗不稳定的情况下。

因此，在实际规划redis这种基础性数据服务时应该注意一下几点：

- 一般不要和其它类型的服务进行混部。
- 同类型的redis服务，也应该针对所服务的不同上层应用进行资源隔离。

说到CPU关联性，可能有人会问是否应该对redis服务进行CPU绑定，以降低由CPU上下文切换带来的性能消耗及关联影响？

简单来说，是可以的，这种优化可以针对任何CPU亲和性要求比较高的服务，但是在此处，有一点我们也应该特别注意：我们在 关于redis内存分析，内存优化 中介绍内存时，曾经提到过子进程内存消耗，也就是redis持久化时会fork出子进程进行AOF/RDB持久化任务。

对于开启了持久化配置的redis服务（一般情况下都会开启），假如我们做了CPU亲和性处理，那么redis fork出的子进程则会和父进程共享同一个CPU资源，我们知道，redis持久化进程是一个非常耗资源的过程，这种自竞争必然会引发redis服务的极大不稳定。

#### 2、内存不在内存了

[关于redis内存分析，内存优化 ](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247484460&idx=1&sn=fbe1377d2e51451311aa910c92de022a&chksm=e80db25adf7a3b4c9d3b38c5c3c73e6ce97dbbcf8c8249acddc452352bf771f28a5ad82c02b1&scene=21#wechat_redirect)开篇就讲过，redis最重要的东西，内存。

内存稳定性是redis提供稳定，低延迟服务的最基本的要求。

然而，我们也知道操作系统有一个 swap 的东西，也就将内存交换到硬盘。假如发生了redis内存被交换到硬盘的情景发生，那么必然，redis服务能力会骤然下降。

swap发现及避免：

##### 1）info memory：

[关于redis内存分析，内存优化](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247484460&idx=1&sn=fbe1377d2e51451311aa910c92de022a&chksm=e80db25adf7a3b4c9d3b38c5c3c73e6ce97dbbcf8c8249acddc452352bf771f28a5ad82c02b1&scene=21#wechat_redirect) 中我们也讲过，swap这种情景，此时，查看redis的内存信息，可以观察到碎片率会小于1。这也可以作为监控redis服务稳定性的一个指标。

##### 2）通过redis进程查看。

首先通过 info server 获取进程id：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDOCnWiacRDLUdrVuHAIUnBSP03Gibkulz5ET9WrceXk6URpTY1qX2Xaow/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

查看 redis 进程 swap 情况：`cat /proc/1686/smaps`

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDN8d05fhN1oicYt7TjNnXY0PyPJFfxo6pGtzt1QfePmhibxbyKfY6agsw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

确定交换量都为0KB或者4KB。

##### 3）redis服务maxmemory配置。

关于redis内存分析，内存优化 中我们提到过，对redis服务必要的内存上限配置，这是内存隔离的一种必要。需要确定的是所有redis实例的分配内存总额小于总的可用物理内存。

##### 4）系统优化：

另外，在最初的基础服务操作系统安装部署时，也需要做一些必要的前置优化，如关闭swap或配置系统尽量避免使用。

#### 3、网络问题

网络问题，是一个普遍的影响因素。

##### 1）网络资源耗尽

简单来说，就是带宽不够了，整个属于基础资源架构的问题了，对网络资源的预估不足，跨机房，异地部署等都会成为诱因。

##### 2）连接数用完了

一个客户端连接对应着一个TCP连接，一个TCP连接在LINUX系统内对应着一个文件句柄，系统级别连接句柄用完了，也就无法再进行连接了。（更多面试题，欢迎关注公众号 Java面试题精选）

查看当前系统限制：`ulimit -n`

设置：`ulimit -n {num}`

##### 3）端口TCP backlog队列满了

linux系统对于每个端口使用backlog保存每一个TCP连接。

redis配置：tcp_backlog 默认511

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDibAvInhmTaOb8qPjFFQhC9WE4ibl7lgKvOPM2n0h5S3VbP3HN4t5OhQg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

高并发情境下，可以适当调整此配置，但需要注意的是，同时要调整系统相关设置。

系统修改命令：`echo {num}>/proc/sys/net/core/somaxconn`

查看因为队列溢出导致的连接绝句：`netstat -s | grep overflowed`

#### ![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDiadriakBvENRltDA3osoC2GFaic4E9lSVOr5hxNoIAQbPPpkqYwichTbNg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##### 4）网络延迟

网络质量问题，可以使用 redis-cli 进行网络状况的测试：

延迟测试：`redis-cli -h {host} -p {port} --latency`

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDsj6mot4SaNUr7LnxQO8s4ANhW6j4VPB9pwgEliar193Qkpc7T6z12Mg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

采样延迟测试：`redis-cli -h {host} -p {port} --latency-history` 默认15s一次

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDDnF7Rg4N9fZTnxnrianGG35KThndXjAkFO2GIDlPfP9qeOJqHo3nDRw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

图形线上测试结果：`redis-cli -h {host} -p {port} --latency-dist`

#### ![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDkawX9LUWlARaQX3Nt2pIt7fAGHuAmazDRF3KlKqMnalSZNh04Qejlw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##### 4）网卡软中断

单个网卡队列只能使用单个CPU资源问题。

### 二、redis 服务使用问题

#### 1、慢查询

如果你的查询总是慢查询，那么必然你的使用存在不合理。

##### 1）你的key规划是否合理

太长或太短都是不建议的，key需要设置的简短而有意义。

##### 2）值类型选择是否合理。

hash还是string，set还是zset，避免大对象存储。

线上可以通过scan命令进行大对象发现治理。

##### 3）是否能够批查询

get 还是 mget；是否应该使用pipeline。

##### 4）禁止线上大数据量操作

#### 2、redis 服务运行状况

查看redis服务运行状况：`redis-cli -h {host} -p {port} --stat`

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGD80NHhTBk0l8Op9q9jvIPUic2VE6m7UtLCXDEYQxP50btXcLDicSQpqEA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

keys：当前key总数；mem：内存使用；clients：当前连接client数；blocked：阻塞数；requests：累计请求数；connections：累计连接数

#### 3、持久化操作影响

##### 1）fork子进程影响

redis 进行持久化操作需要fork出子进程。fork子进程本身如果时间过长，则会产生一定的影响。

查看命令最近一次fork耗时：`info stats`

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBGSm3hicge6M5g5mtECxgGDQEQawHUsI4iawwmXjBmDC97pU9yyXRCCvx188M9Zfv6DZx1KYiawrf3A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

单位微妙，确保不要超过1s。

##### 2）AOF刷盘阻塞

AOF持久化开启，后台每秒进行AOF文件刷盘操作，系统fsync操作将AOF文件同步到硬盘，如果主线程发现距离上一次成功fsync超过2s，则会阻塞后台线程等待fsync完成以保障数据安全性。

##### 3）THP问题

关于redis内存分析，内存优化 中我们讲过透明大页问题，linux系统的写时复制机制会使得每次写操作引起的页复制由4KB提升至2M从而导致写慢查询。如果慢查询堆积必然导致后续连接问题。

## 06.说一下使用 Redis 实现大规模的帖子浏览计数的思路

### 统计方法

我们对统计浏览量有四个基本的要求

- 计数必须达到实时或者接近实时。
- 每个用户在一个时间窗口内仅被记录一次。
- 帖子显示的统计数量的误差不能超过百分之几。
- 整个系统必须能在生成环境下，数秒内完成阅读计数的处理。

满足上面四个条件，其实比想象中要复杂。为了在实时统计的情况下保持精准度，我们需要知道某一个用户之前是否浏览过一篇文章，所以我们需要为每一篇文章存储浏览过它的用户的集合，并且在每次新增浏览时检查该集合进行去重复操作。

一个比较简单的解决方案是，为每篇文章维护一个哈希表，用文章ID作为key，去重的userid的集合(set数据结构)作为value。

这种方案在文章数量和阅读数比较小的情况下，还能很好的运行，但当数据量到达大规模时，它就不适用了。尤其是该文章变成了热门文章，阅读数迅速增长，有些受欢迎的文章的阅读者数量超过百万级别，想象一下维护一个超过百万的unqine userId的集合在内存中的，还有经受住不断的查询，集合中的用户是否存在。

自从我们决定不提供100%精准的数据后，我们开始考虑使用几种不同的基数估计算法。我们综合考虑下选出量两个可以满足需求的算法：

- 线性概率计算方法，它非常精确，但是需要的内存数量是根据用户数线性增长的。
- 基于HyperLogLog (HLL)的计算方法，HLL的内存增长是非线性的，但是统计的精准度和线性概率就不是同一级别的了。

为了更好的理解基于HLL的计算方法，究竟能够节省多少内存，我们这里使用一个例子。

考虑到r/pics文章，在本文开头提及，该文章收到了超过一百万用户的浏览过，如果我们存储一百万个唯一的用户ID，每一个id占用8个字节，那么仅仅一篇文章就需要8mb的空间存储！对照着HLL所需要的存储空间就非常少了，在这个例子中使用HLL计算方法仅需要 12kb的空间也就是第一种方法的0.15%。

(This article on High Scalability 这篇文章讲解了上面的两种算法.)

有很多的HLL实现是基于上面两种算法的结合而成的，也就是一开始统计数量少的情况下使用线性概率方法，当数量达到一定阈值时，切换为HLL方法。这种混合方法非常有用，不但能够为小量数据集提供精准性，也能为大量数据节省存储空间。该种实现方式的细节请参阅论文（Google’s HyperLogLog++ paper）

HLL算法的实现是相当标准的，这里有三种不同的实现方式，要注意的是，基于内存存储方案的HLL，这里我们只考虑Java和Scale两种实现

- Twitter的**Algebird**库，**Scala**实现，Algebird的文档撰写非常好，但是关于它是如何实现HLL的，不是很容易理解。
- stream-lib库中的**HyperLogLog++**实现，Java编写。stream-lib代码的文档化做的很好，但我们对如何适当调优它，还是有些困惑的。
- **Redis**的HLL实现(我们最终的选择)，我们觉得Redis的实现不管从文档完善程度还是配置和提供的API接口，来说做的都非常好。另外的加分点是，使用Redis可以减少我们对CPU和内存性能的担忧。





![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XBKMELrFL6QVsO242OdPanAZV19rRhtF5A7iaZMcKiaqe0X1d360LoR2AEaj3XDY7icRXk1mxzWsRh1A/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)





Reddit的数据管道，主要都是使用Apache Kafka的。每当一个用户浏览一篇文章时，就会触发一个事件并且被发送到事件收集服务器，然后批量的将这些事件发送打kafka中进行持久化。

Reddit的浏览统计系统，分为两个顺序执行的组成部分，其中的第一部分是，被称为**Nazar**的**kafka**队列『消费者』(consumer) ，它会从kafka中读取事件，然后将这些事件通过特定的条件进行过滤，判断改事件是否应该被算作一次文章阅读计数，它被称为『NAZAR』是因为在系统中它有作为『眼镜』的用处，识别出哪些事件是不应该被加入到统计中的。

**Nazar**使用**Redis** 维护状态还有一个事件不被计数的潜在原因，**这个原因可能是用户短时间内重复浏览统一文章**。Nazar会在事件被发送回kafka时，为事件添加一个标识位，根据该事件是否被加入到计数当中的布尔值。

统计系统的第二部是一个称为Abacus 的kafka『消费者』它会真正的统计浏览量，并且让浏览量数据可以在整站和客户端上显示， 它接收从Nazar发送出来的事件消息，然后根据该消息中包含着标识值（**Nazar**中处理的）来判断这个事件是否算做一次计数，如果事件被计数，Abacus会首先检查这个事件中文章的HLL计数是否存在于Redis中，如果存在，Abacus会发送一个PFADD请求给Redis，如果不存在，Abacus会发生一个请求到Cassandra集群，Cassandra集群会持久化HLL 计数和真实的原始计数数据，然后再发送一个SET请求到Redis，这个过程通常出现在用户阅读一个已经被Redis剔除的就文章的情况下发送。

为了让维护一个在Redis可能被剔除的旧文章，Abacus会定期的，从Redis中将HLL过滤数据，包括每篇文章的计数，全部写入到Cassandra集群中，当然为了避免集群过载，这个步骤会分为每篇文章10秒一组批次进行写入。下图就是整个过程的流程图。



![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XBKMELrFL6QVsO242OdPanAnksLIfqzI8IZQvryZvXoaYYIdG9B1WX3UCfG234rqr0Sg8cHDPhWuQ/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



> 来源：https://www.jianshu.com/p/523635f5f133

## 07.Redis和MongoDB的区别

Redis主要把数据存储在内存中，其“缓存”的性质远大于其“数据存储“的性质，其中数据的增删改查也只是像变量操作一样简单；

MongoDB却是一个“存储数据”的系统，增删改查可以添加很多条件，就像SQL数据库一样灵活，这一点在面试的时候很受用。

MongoDB语法与现有关系型数据库SQL语法比较

> https://www.cnblogs.com/java-spring/p/9488200.html

### Mongodb与Redis应用指标对比

MongoDB和Redis都是NoSQL，采用结构型数据存储。二者在使用场景中，存在一定的区别，这也主要由于二者在内存映射的处理过程，持久化的处理方法不同。MongoDB建议集群部署，更多的考虑到集群方案，Redis更偏重于进程顺序写入，虽然支持集群，也仅限于主-从模式。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XDZw9HWiar2kjLKI2LUbWC74DwsceSbrBZgAE1c8ug1CIT8KvPFibDx9ibk2rg0UuAicTZ3FZLSghVpNA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



> 来源：cnblogs.com/java-spring/p/9488227.html

## 08.说说Redis的过期键删除策略吧

对于Redis服务器来说，内存资源非常宝贵，如果一些过期键一直不被删除，就会造成资源浪费，因此我们需要考虑一个问题：如果一个键过期了，它什么时候会被删除呢？

### 1. 常见的删除策略

常见的删除策略有以下3种：

- **定时删除**

在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。

- **惰性删除**

放任过期键不管，每次从键空间中获取键时，检查该键是否过期，如果过期，就删除该键，如果没有过期，就返回该键。

- **定期删除**

每隔一段时间，程序对数据库进行一次检查，删除里面的过期键，至于要删除哪些数据库的哪些过期键，则由算法决定。

其中定时删除和定期删除为主动删除策略，惰性删除为被动删除策略。

接下来我们一一讲解。

#### 1.1 定时删除策略

定时删除策略通过使用定时器，定时删除策略可以保证过期键尽可能快地被删除，并释放过期键占用的内存。

因此，定时删除策略的优缺点如下所示：

- 优点：对内存非常友好
- 缺点：对CPU时间非常不友好

举个例子，如果有大量的命令请求等待服务器处理，并且服务器当前不缺少内存，如果服务器将大量的CPU时间用来删除过期键，那么服务器的响应时间和吞吐量就会受到影响。

也就是说，如果服务器创建大量的定时器，服务器处理命令请求的性能就会降低，因此Redis目前并没有使用定时删除策略。

#### 1.2 惰性删除策略

惰性删除策略只会在获取键时才对键进行过期检查，不会在删除其它无关的过期键花费过多的CPU时间。

因此，惰性删除策略的优缺点如下所示：

- 优点：对CPU时间非常友好
- 缺点：对内存非常不友好

举个例子，如果数据库有很多的过期键，而这些过期键又恰好一直没有被访问到，那这些过期键就会一直占用着宝贵的内存资源，造成资源浪费。

#### 1.3 定期删除策略

定期删除策略是定时删除策略和惰性删除策略的一种整合折中方案。

定期删除策略每隔一段时间执行一次删除过期键操作，并通过限制删除操作执行的时长和频率来减少删除操作对CPU时间的影响，同时，通过定期删除过期键，也有效地减少了因为过期键而带来的内存浪费。

### 2. Redis使用的过期键删除策略

Redis服务器使用的是惰性删除策略和定期删除策略。

#### 2.1 惰性删除策略的实现

过期键的惰性删除策略由expireIfNeeded函数实现，所有读写数据库的Redis命令在执行之前都会调用expireIfNeeded函数对输入键进行检查：

- 如果输入键已经过期，那么将输入键从数据库中删除
- 如果输入键未过期，那么不做任何处理

以上描述可以使用如下流程图表示：

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XC0Ve0Oj9m2ibicQ5PUvzHfbibsk09OWN8KdaPelkI9IkR1WpetRGyOr7OjSOC5fYjuwdAuSJScgVB9A/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 2.2 定期删除策略的实现

过期键的定期删除策略由activeExpireCycle函数实现，每当Redis服务器的周期性操作serverCron函数执行时，activeExpireCycle函数就会被调用，它在规定的时间内，分多次遍历服务器中的各个数据库，从数据库的expires字典中随机检查一部分键的过期时间，并删除其中的过期键。

activeExpireCycle函数的大体流程为：

> 函数每次运行时，都从一定数量的数据库中随机取出一定数量的键进行检查，并删除其中的过期键，比如先从0号数据库开始检查，下次函数运行时，可能就是从1号数据库开始检查，直到15号数据库检查完毕，又重新从0号数据库开始检查，这样可以保证每个数据库都被检查到。

划重点：

关于定期删除的大体流程，最近面试时有被问道，我就是按上述描述回答的。

可能有的面试官还会问，每次随机删除哪些key呢？可以提下LRU算法（Least Recently Used 最近最少使用），一般不会再细问，不过有兴趣的同学可以深入研究下。更多面试题，欢迎关注公众号 Java面试题精选

### 3. RDB对过期键的处理

#### 3.1 生成RDB文件

在执行SAVE命令或者BGSAVE命令创建一个新的RDB文件时，程序会对数据库中的键进行检查，已过期的键不会被保存到新创建的RDB文件中。

举个例子，如果数据库中包含3个键k1、k2、k3，并且k2已经过期，那么创建新的RDB文件时，程序只会将k1和k3保存到RDB文件中，k2则会被忽略。

#### 3.2 载入RDB文件

在启动Redis服务器时，如果服务器只开启了RDB持久化，那么服务器将会载入RDB文件：

- 如果服务器以主服务器模式运行，在载入RDB文件时，程序会对文件中保存的键进行检查，未过期的键会被载入到数据库中，过期键会被忽略。
- 如果服务器以从服务器模式运行，在载入RDB文件时，文件中保存的所有键，不论是否过期，都会被载入到数据库中。

因为主从服务器在进行数据同步（完整重同步）的时候，从服务器的数据库会被清空，所以一般情况下，过期键对载入RDB文件的从服务器不会造成影响。更多面试题，欢迎关注公众号 Java面试题精选

### 4. AOF对过期键的处理

#### 4.1 AOF文件写入

如果数据库中的某个键已经过期，并且服务器开启了AOF持久化功能，当过期键被惰性删除或者定期删除后，程序会向AOF文件追加一条DEL命令，显式记录该键已被删除。

举个例子，如果客户端执行命令GET message访问已经过期的message键，那么服务器将执行以下3个动作：

- 从数据库中删除message键
- 追加一条DEL message命令到AOF文件
  -向执行GET message命令的客户端返回空回复

#### 4.2 AOF文件重写

在执行AOF文件重写时，程序会对数据库中的键进行检查，已过期的键不会被保存到重写后的AOF文件中。

### 5. 复制功能对过期键的处理

在主从复制模式下，从服务器的过期键删除动作由主服务器控制：

- 主服务器在删除一个过期键后，会显式地向所有从服务器发送一个DEL命令，告知从服务器删除这个过期键。
- 从服务器在执行客户端发送的读命令时，即使发现该键已过期也不会删除该键，照常返回该键的值。
- 从服务器只有接收到主服务器发送的DEL命令后，才会删除过期键。

## 09.Redis的字符串是怎么实现的

### Redis字符串的实现

Redis虽然是用C语言写的，但却没有直接用C语言的字符串，而是自己实现了一套字符串。目的就是为了提升速度，提升性能，可以看出Redis为了高性能也是煞费苦心。

Redis构建了一个叫做简单动态字符串（Simple Dynamic String），简称SDS

#### 1.SDS 代码结构

```
struct sdshdr{
    //  记录已使用长度
    int len;
    // 记录空闲未使用的长度
    int free;
    // 字符数组
    char[] buf;
};
```

SDS ？什么鬼？可能对此陌生的朋友对这个名称有疑惑。只是个名词而已不必在意，我们要重点欣赏借鉴Redis的设计思路。下面画个图来说明，一目了然。

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XCsqyWO8pRHIiaObEX7mA2RYXae1TicrODmoI20icdJVp447NQ7j05pbVlEmWDWCd2yOMjxWmczTAnmQ/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

Redis的字符串也会遵守C语言的字符串的实现规则，即最后一个字符为空字符。然而这个空字符不会被计算在len里头。

#### 2.SDS 动态扩展特点

SDS的最厉害最奇妙之处在于它的Dynamic。动态变化长度。举个例子

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XCsqyWO8pRHIiaObEX7mA2RYF3I2tWEPQc7Nn1OGExkbERltTH7qr8Vic3RLygSHibkepvUlQ4pzQaibQ/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如上图所示刚开始s1 只有5个空闲位子，后面需要追加' world' 6个字符，很明显是不够的。那咋办？Redis会做以下三个操作：

1. 计算出大小是否足够
2. 开辟空间至满足所需大小
3. 开辟与已使用大小len相同长度的空闲free空间（如果len < 1M）开辟1M长度的空闲free空间（如果len >= 1M）

看到这儿为止有没有朋友觉得这个实现跟Java的列表List实现有点类似呢？看完后面的会觉得更像了。

### Redis字符串的性能优势

- 快速获取字符串长度
- 避免缓冲区溢出
- 降低空间分配次数提升内存使用效率

#### 1.快速获取字符串长度

再看下上面的SDS结构体：

```c
struct sdshdr{
    //  记录已使用长度
    int len;
    // 记录空闲未使用的长度
    int free;
    // 字符数组
    char[] buf;
};
```

由于在SDS里存了已使用字符长度len，所以当想获取字符串长度时直接返回len即可，时间复杂度为O(1)。如果使用C语言的字符串的话它的字符串长度获取函数时间复杂度为O(n),n为字符个数，因为他是从头到尾（到空字符'\0'）遍历相加。

#### 2.避免缓冲区溢出

对一个C语言字符串进行strcat追加字符串的时候需要提前开辟需要的空间，如果不开辟空间的话可能会造成缓冲区溢出，而影响程序其他代码。如下图，有一个字符串s1="hello" 和 字符串s2="baby",现在要执行strcat(s1,"world"),并且执行前未给s1开辟空间，所以造成了缓冲区溢出。

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XCsqyWO8pRHIiaObEX7mA2RYWYdrgTj2vFkJod1xCRAkYlNjs7qQgwMa1TMDjlTPtDemC1cichEsTibw/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

而对于Redis而言由于每次追加字符串时都会检查空间是否够用，所以不会存在缓冲区溢出问题。每次追加操作前都会做如下操作：

1. 计算出大小是否足够
2. 开辟空间至满足所需大小

#### 3.降低空间分配次数提升内存使用效率

字符串的追加操作会涉及到内存分配问题，然而内存分配问题会牵扯内存划分算法以及系统调用所以如果频繁发生的话影响性能，所以对于性能至上的Redis来说这是万万不能忍受的。所以采取了以下两种优化措施

- 空间与分配
- 惰性空间回收

**1. 空间预分配**

对于追加操作来说，Redis不仅会开辟空间至够用而且还会预分配未使用的空间(free)来用于下一次操作。至于未使用的空间(free)的大小则由修改后的字符串长度决定。

当修改后的字符串长度len < 1M,则会分配与len相同长度的未使用的空间(free)

当修改后的字符串长度len >= 1M,则会分配1M长度的未使用的空间(free)

有了这个预分配策略之后会减少内存分配次数，因为分配之前会检查已有的free空间是否够，如果够则不开辟了～

**2. 惰性空间回收**

与上面情况相反，惰性空间回收适用于字符串缩减操作。比如有个字符串s1="hello world"，对s1进行sdstrim(s1," world")操作，执行完该操作之后Redis不会立即回收减少的部分，而是会分配给下一个需要内存的程序。当然，Redis也提供了回收内存的api,可以自己手动调用来回收缩减部分的内存。

到此为止结束了～

下次在遇到这个问题可以侃侃而谈了，哈哈哈

> 来源：juejin.im/post/5ca9d8ae6fb9a05e5c05c4e8

## 10.什么是 redis 的雪崩、穿透和击穿？redis 崩溃之后会怎么样？应对措施是什么

### 面试题

了解什么是 redis 的雪崩、穿透和击穿？redis 崩溃之后会怎么样？系统该如何应对这种情况？如何处理 redis 的穿透？

### 面试官心理分析

其实这是问到缓存必问的，因为缓存雪崩和穿透，是缓存最大的两个问题，要么不出现，一旦出现就是致命性的问题，所以面试官一定会问你。

### 面试题剖析

#### 缓存雪崩

对于系统 A，假设每天高峰期每秒 5000 个请求，本来缓存在高峰期可以扛住每秒 4000 个请求，但是缓存机器意外发生了全盘宕机。缓存挂了，此时 1 秒 5000 个请求全部落数据库，数据库必然扛不住，它会报一下警，然后就挂了。此时，如果没有采用什么特别的方案来处理这个故障，DBA 很着急，重启数据库，但是数据库立马又被新的流量给打死了。

这就是缓存雪崩。







![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XDgEqLXadxb8EXsfM7sDdiasaicdbYwcflkG63lVmQ17JOAh5c2Ibib3xTvkITbanwsBDggib3Uf3RD5Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



redis-caching-avalanche



大约在 3 年前，国内比较知名的一个互联网公司，曾因为缓存事故，导致雪崩，后台系统全部崩溃，事故从当天下午持续到晚上凌晨 3~4 点，公司损失了几千万。

缓存雪崩的事前事中事后的解决方案如下：

- 事前：redis 高可用，主从+哨兵，redis cluster，避免全盘崩溃。
- 事中：本地 ehcache 缓存 + hystrix 限流&降级，避免 MySQL 被打死。
- 事后：redis 持久化，一旦重启，自动从磁盘上加载数据，快速恢复缓存数据。







![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XDgEqLXadxb8EXsfM7sDdias1XyMPK5yicCMGvD3HKUcx8eziabN6AxbccO3IMP18aFX7trQqLc3dadQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



redis-caching-avalanche-solution



用户发送一个请求，系统 A 收到请求后，先查本地 ehcache 缓存，如果没查到再查 redis。如果 ehcache 和 redis 都没有，再查数据库，将数据库中的结果，写入 ehcache 和 redis 中。

限流组件，可以设置每秒的请求，有多少能通过组件，剩余的未通过的请求，怎么办？**走降级**！可以返回一些默认的值，或者友情提示，或者空白的值。

好处：

- 数据库绝对不会死，限流组件确保了每秒只有多少个请求能通过。
- 只要数据库不死，就是说，对用户来说，2/5 的请求都是可以被处理的。
- 只要有 2/5 的请求可以被处理，就意味着你的系统没死，对用户来说，可能就是点击几次刷不出来页面，但是多点几次，就可以刷出来一次。

#### 缓存穿透

对于系统A，假设一秒 5000 个请求，结果其中 4000 个请求是黑客发出的恶意攻击。

黑客发出的那 4000 个攻击，缓存中查不到，每次你去数据库里查，也查不到。

举个栗子。数据库 id 是从 1 开始的，结果黑客发过来的请求 id 全部都是负数。这样的话，缓存中不会有，请求每次都“**视缓存于无物**”，直接查询数据库。这种恶意攻击场景的缓存穿透就会直接把数据库给打死。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XDgEqLXadxb8EXsfM7sDdiasDZqhtSQVa0Gv8cyicaDqONYXXCBXviafhBj0ASIL5eNWsSyeXAViaHZ6g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



redis-caching-penetration



解决方式很简单，每次系统 A 从数据库中只要没查到，就写一个空值到缓存里去，比如 `set -999 UNKNOWN`。然后设置一个过期时间，这样的话，下次有相同的 key 来访问的时候，在缓存失效之前，都可以直接从缓存中取数据。

#### 缓存击穿

缓存击穿，就是说**某个 key 非常热点，访问非常频繁，处于集中式高并发访问的情况，当这个 key 在失效的瞬间，大量的请求就击穿了缓存，直接请求数据库**，就像是在一道屏障上凿开了一个洞。

不同场景下的解决方式可如下：

- 若缓存的数据是基本不会发生更新的，则可尝试将该热点数据设置为永不过期。
- 若缓存的数据更新不频繁，且缓存刷新的整个流程耗时较少的情况下，则可以采用基于 redis、zookeeper 等分布式中间件的分布式互斥锁，或者本地互斥锁以保证仅少量的请求能请求数据库并重新构建缓存，其余线程则在锁释放后能访问到新缓存。
- 若缓存的数据更新频繁或者缓存刷新的流程耗时较长的情况下，可以利用定时线程在缓存过期前主动的重新构建缓存或者延后缓存的过期时间，以保证所有的请求能一直访问到对应的缓存。

> 来源：https://github.com/doocs/advanced-java

## 11.谈谈 Redis 的过期策略

在日常开发中，我们使用 Redis 存储 key 时通常会设置一个过期时间，但是 Redis 是怎么删除过期的 key，而且 Redis 是单线程的，删除 key 会不会造成阻塞。要搞清楚这些，就要了解 Redis 的过期策略和内存淘汰机制。

**Redis采用的是定期删除 + 懒惰删除策略。**

### 定期删除策略

Redis 会将每个设置了过期时间的 key 放入到一个独立的字典中，默认每 100ms 进行一次过期扫描：

1. 随机抽取 20 个 key
2. 删除这 20 个key中过期的key
3. 如果过期的 key 比例超过 1/4，就重复步骤 1，继续删除。

**为什不扫描所有的 key？**

Redis 是单线程，全部扫描岂不是卡死了。而且为了防止每次扫描过期的 key 比例都超过 1/4，导致不停循环卡死线程，Redis 为每次扫描添加了上限时间，默认是 25ms。

如果客户端将超时时间设置的比较短，比如 10ms，那么就会出现大量的链接因为超时而关闭，业务端就会出现很多异常。而且这时你还无法从 Redis 的 slowlog 中看到慢查询记录，因为慢查询指的是逻辑处理过程慢，不包含等待时间。

如果在同一时间出现大面积 key 过期，Redis 循环多次扫描过期词典，直到过期的 key 比例小于 1/4。这会导致卡顿，而且在高并发的情况下，可能会导致缓存雪崩。

**为什么 Redis 为每次扫描添的上限时间是 25ms，还会出现上面的情况？**

因为 Redis 是单线程，每个请求处理都需要排队，而且由于 Redis 每次扫描都是 25ms，也就是每个请求最多 25ms，100 个请求就是 2500ms。

如果有大批量的 key 过期，要给过期时间设置一个随机范围，而不宜全部在同一时间过期，分散过期处理的压力。

### 从库的过期策略

从库不会进行过期扫描，从库对过期的处理是被动的。主库在 key 到期时，会在 AOF 文件里增加一条 del 指令，同步到所有的从库，从库通过执行这条 del 指令来删除过期的 key。

因为指令同步是异步进行的，所以主库过期的 key 的 del 指令没有及时同步到从库的话，会出现主从数据的不一致，主库没有的数据在从库里还存在。

### 懒惰删除策略

**Redis 为什么要懒惰删除(lazy free)？**

删除指令 del 会直接释放对象的内存，大部分情况下，这个指令非常快，没有明显延迟。不过如果删除的 key 是一个非常大的对象，比如一个包含了千万元素的 hash，又或者在使用 FLUSHDB 和 FLUSHALL 删除包含大量键的数据库时，那么删除操作就会导致单线程卡顿。

redis 4.0 引入了 lazyfree 的机制，它可以将删除键或数据库的操作放在后台线程里执行， 从而尽可能地避免服务器阻塞。

#### unlink

unlink 指令，它能对删除操作进行懒处理，丢给后台线程来异步回收内存。

```
> unlink key
OK
```

#### flush

flushdb 和 flushall 指令，用来清空数据库，这也是极其缓慢的操作。Redis 4.0 同样给这两个指令也带来了异步化，在指令后面增加 async 参数就可以将整棵大树连根拔起，扔给后台线程慢慢焚烧。

```
> flushall async
OK
```

#### 异步队列

主线程将对象的引用从「大树」中摘除后，会将这个 key 的内存回收操作包装成一个任务，塞进异步任务队列，后台线程会从这个异步队列中取任务。任务队列被主线程和异步线程同时操作，所以必须是一个线程安全的队列。

不是所有的 unlink 操作都会延后处理，如果对应 key 所占用的内存很小，延后处理就没有必要了，这时候 Redis 会将对应的 key 内存立即回收，跟 del 指令一样。

#### 更多异步删除点

Redis 回收内存除了 del 指令和 flush 之外，还会存在于在 key 的过期、LRU 淘汰、rename 指令以及从库全量同步时接受完 rdb 文件后会立即进行的 flush 操作。

Redis4.0 为这些删除点也带来了异步删除机制，打开这些点需要额外的配置选项。

- slave-lazy-flush 从库接受完 rdb 文件后的 flush 操作
- lazyfree-lazy-eviction 内存达到 maxmemory 时进行淘汰
- lazyfree-lazy-expire key 过期删除
- lazyfree-lazy-server-del rename 指令删除 destKey

### 内存淘汰机制

Redis 的内存占用会越来越高。Redis 为了限制最大使用内存，提供了 redis.conf 中的
配置参数 maxmemory。当内存超出 maxmemory，**Redis 提供了几种内存淘汰机制让用户选择，配置 maxmemory-policy：**

- **noeviction：**当内存超出 maxmemory，写入请求会报错，但是删除和读请求可以继续。（使用这个策略，疯了吧）
- **allkeys-lru：**当内存超出 maxmemory，在所有的 key 中，移除最少使用的key。只把 Redis 既当缓存是使用这种策略。（推荐）。
- **allkeys-random：**当内存超出 maxmemory，在所有的 key 中，随机移除某个 key。（应该没人用吧）
- **volatile-lru：**当内存超出 maxmemory，在设置了过期时间 key 的字典中，移除最少使用的 key。把 Redis 既当缓存，又做持久化的时候使用这种策略。
- **volatile-random：**当内存超出 maxmemory，在设置了过期时间 key 的字典中，随机移除某个key。
- **volatile-ttl：**当内存超出 maxmemory，在设置了过期时间 key 的字典中，优先移除 ttl 小的。

### LRU 算法

实现 LRU 算法除了需要 key/value 字典外，还需要附加一个链表，链表中的元素按照一定的顺序进行排列。当空间满的时候，会踢掉链表尾部的元素。当字典的某个元素被访问时，它在链表中的位置会被移动到表头。所以链表的元素排列顺序就是元素最近被访问的时间顺序。

使用 Python 的 OrderedDict(双向链表 + 字典) 来实现一个简单的 LRU 算法：

```
from collections import OrderedDict

class LRUDict(OrderedDict):

    def __init__(self, capacity):
        self.capacity = capacity
        self.items = OrderedDict()

    def __setitem__(self, key, value):
        old_value = self.items.get(key)
        if old_value is not None:
            self.items.pop(key)
            self.items[key] = value
        elif len(self.items) < self.capacity:
            self.items[key] = value
        else:
            self.items.popitem(last=True)
            self.items[key] = value

    def __getitem__(self, key):
        value = self.items.get(key)
        if value is not None:
            self.items.pop(key)
            self.items[key] = value
        return value

    def __repr__(self):
        return repr(self.items)


d = LRUDict(10)

for i in range(15):
    d[i] = i
print d
```

#### 近似 LRU 算法

Redis 使用的并不是完全 LRU 算法。不使用 LRU 算法，是为了节省内存，Redis 采用的是随机LRU算法，Redis 为每一个 key 增加了一个24 bit的字段，用来记录这个 key 最后一次被访问的时间戳。

注意 Redis 的 LRU 淘汰策略是懒惰处理，也就是不会主动执行淘汰策略，当 Redis 执行写操作时，发现内存超出 maxmemory，就会执行 LRU 淘汰算法。这个算法就是随机采样出5(默认值)个 key，然后移除最旧的 key，如果移除后内存还是超出 maxmemory，那就继续随机采样淘汰，直到内存低于 maxmemory 为止。

如何采样就是看 maxmemory-policy 的配置，如果是 allkeys 就是从所有的 key 字典中随机，如果是 volatile 就从带过期时间的 key 字典中随机。每次采样多少个 key 看的是 maxmemory_samples 的配置，默认为 5。

#### LFU

Redis 4.0 里引入了一个新的淘汰策略 —— LFU（Least Frequently Used） 模式，作者认为它比 LRU 更加优秀。

LFU 表示按最近的访问频率进行淘汰，它比 LRU 更加精准地表示了一个 key 被访问的热度。

如果一个 key 长时间不被访问，只是刚刚偶然被用户访问了一下，那么在使用 LRU 算法下它是不容易被淘汰的，因为 LRU 算法认为当前这个 key 是很热的。而 LFU 是需要追踪最近一段时间的访问频率，如果某个 key 只是偶然被访问一次是不足以变得很热的，它需要在近期一段时间内被访问很多次才有机会被认为很热。

**Redis 对象的热度**

Redis 的所有对象结构头中都有一个 24bit 的字段，这个字段用来记录对象的热度。

```
// redis 的对象头
typedef struct redisObject {
    unsigned type:4; // 对象类型如 zset/set/hash 等等
    unsigned encoding:4; // 对象编码如 ziplist/intset/skiplist 等等
    unsigned lru:24; // 对象的「热度」
    int refcount; // 引用计数
    void *ptr; // 对象的 body
} robj;
```

**LRU 模式**

在 LRU 模式下，lru 字段存储的是 Redis 时钟 server.lruclock，Redis 时钟是一个 24bit 的整数，默认是 Unix 时间戳对 2^24 取模的结果，大约 97 天清零一次。当某个 key 被访问一次，它的对象头的 lru 字段值就会被更新为 server.lruclock。

**LFU 模式**

在 LFU 模式下，lru 字段 24 个 bit 用来存储两个值，分别是 ldt(last decrement time) 和 logc(logistic counter)。

logc 是 8 个 bit，用来存储访问频次，因为 8 个 bit 能表示的最大整数值为 255，存储频次肯定远远不够，所以这 8 个 bit 存储的是频次的对数值，并且这个值还会随时间衰减。如果它的值比较小，那么就很容易被回收。为了确保新创建的对象不被回收，新对象的这 8 个 bit 会初始化为一个大于零的值，默认是 LFU_INIT_VAL=5。

ldt 是 16 个位，用来存储上一次 logc 的更新时间，因为只有 16 位，所以精度不可能很高。它取的是分钟时间戳对 2^16 进行取模，大约每隔 45 天就会折返。

同 LRU 模式一样，我们也可以使用这个逻辑计算出对象的空闲时间，只不过精度是分钟级别的。图中的 server.unixtime 是当前 redis 记录的系统时间戳，和 server.lruclock 一样，它也是每毫秒更新一次。

## 12.Redis中是如何实现分布式锁的？

分布式锁常见的三种实现方式：

1. 数据库乐观锁；
2. 基于Redis的分布式锁；
3. 基于ZooKeeper的分布式锁。

本地面试考点是，你对Redis使用熟悉吗？Redis中是如何实现分布式锁的。

### 要点

Redis要实现分布式锁，以下条件应该得到满足

**互斥性**

- 在任意时刻，只有一个客户端能持有锁。

**不能死锁**

- 客户端在持有锁的期间崩溃而没有主动解锁，也能保证后续其他客户端能加锁。

**容错性**

- 只要大部分的Redis节点正常运行，客户端就可以加锁和解锁。

### 实现

可以直接通过 `set key value px milliseconds nx` 命令实现加锁， 通过Lua脚本实现解锁。

```
//获取锁（unique_value可以是UUID等）
SET resource_name unique_value NX PX  30000

//释放锁（lua脚本中，一定要比较value，防止误解锁）
if redis.call("get",KEYS[1]) == ARGV[1] then
    return redis.call("del",KEYS[1])
else
    return 0
end
```

**代码解释**

- set 命令要用 `set key value px milliseconds nx`，替代 `setnx + expire` 需要分两次执行命令的方式，保证了原子性，
- value 要具有唯一性，可以使用`UUID.randomUUID().toString()`方法生成，用来标识这把锁是属于哪个请求加的，在解锁的时候就可以有依据；
- 释放锁时要验证 value 值，防止误解锁；
- 通过 Lua 脚本来避免 Check And Set 模型的并发问题，因为在释放锁的时候因为涉及到多个Redis操作 （利用了eval命令执行Lua脚本的原子性）；

**加锁代码分析**

首先，set()加入了NX参数，可以保证如果已有key存在，则函数不会调用成功，也就是只有一个客户端能持有锁，满足互斥性。其次，由于我们对锁设置了过期时间，即使锁的持有者后续发生崩溃而没有解锁，锁也会因为到了过期时间而自动解锁（即key被删除），不会发生死锁。最后，因为我们将value赋值为requestId，用来标识这把锁是属于哪个请求加的，那么在客户端在解锁的时候就可以进行校验是否是同一个客户端。

**解锁代码分析**

将Lua代码传到jedis.eval()方法里，并使参数KEYS[1]赋值为lockKey，ARGV[1]赋值为requestId。在执行的时候，首先会获取锁对应的value值，检查是否与requestId相等，如果相等则解锁（删除key）。

**存在的风险**

如果存储锁对应key的那个节点挂了的话，就可能存在丢失锁的风险，导致出现多个客户端持有锁的情况，这样就不能实现资源的独享了。

1. 客户端A从master获取到锁
2. 在master将锁同步到slave之前，master宕掉了（Redis的主从同步通常是异步的）。
   主从切换，slave节点被晋级为master节点
3. 客户端B取得了同一个资源被客户端A已经获取到的另外一个锁。导致存在同一时刻存不止一个线程获取到锁的情况。

### redlock算法出现

这个场景是假设有一个 redis cluster，有 5 个 redis master 实例。然后执行如下步骤获取一把锁：

1. 获取当前时间戳，单位是毫秒；
2. 跟上面类似，轮流尝试在每个 master 节点上创建锁，过期时间较短，一般就几十毫秒；
3. 尝试在大多数节点上建立一个锁，比如 5 个节点就要求是 3 个节点 n / 2 + 1；
4. 客户端计算建立好锁的时间，如果建立锁的时间小于超时时间，就算建立成功了；
5. 要是锁建立失败了，那么就依次之前建立过的锁删除；
6. 只要别人建立了一把分布式锁，你就得不断轮询去尝试获取锁。



![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7gBGPF5ox7cT8R3YibPiaXmdkSSXz5bL4wDlcbVpiabHpca2cqF5hNqANSAgbQAqbxZVHKlC0N9FHQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

Redis 官方给出了以上两种基于 Redis 实现分布式锁的方法，详细说明可以查看：

> https://redis.io/topics/distlock 。

### Redisson实现

Redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还实现了可重入锁（Reentrant Lock）、公平锁（Fair Lock、联锁（MultiLock）、 红锁（RedLock）、 读写锁（ReadWriteLock）等，还提供了许多分布式服务。

Redisson提供了使用Redis的最简单和最便捷的方法。Redisson的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上。

**Redisson 分布式重入锁用法**

Redisson 支持单点模式、主从模式、哨兵模式、集群模式，这里以单点模式为例：

```
// 1.构造redisson实现分布式锁必要的Config
Config config = new Config();
config.useSingleServer().setAddress("redis://127.0.0.1:5379").setPassword("123456").setDatabase(0);
// 2.构造RedissonClient
RedissonClient redissonClient = Redisson.create(config);
// 3.获取锁对象实例（无法保证是按线程的顺序获取到）
RLock rLock = redissonClient.getLock(lockKey);
try {
    /**
     * 4.尝试获取锁
     * waitTimeout 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败
     * leaseTime   锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完）
     */
    boolean res = rLock.tryLock((long)waitTimeout, (long)leaseTime, TimeUnit.SECONDS);
    if (res) {
        //成功获得锁，在这里处理业务
    }
} catch (Exception e) {
    throw new RuntimeException("aquire lock fail");
}finally{
    //无论如何, 最后都要解锁
    rLock.unlock();
}
```

加锁流程图

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7gBGPF5ox7cT8R3YibPiaXmoorknIfS8DfpgUrLLPFZwGjCG2z6EEMIt22bFPLCuJKLzhH8jbBo3g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

解锁流程图

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7gBGPF5ox7cT8R3YibPiaXmBibGxBgFjUiagH8viaukmPOwPYKZyNrHeuY5H9PkD70vDsic6NMKopMrtg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

我们可以看到，RedissonLock是可重入的，并且考虑了失败重试，可以设置锁的最大等待时间， 在实现上也做了一些优化，减少了无效的锁申请，提升了资源的利用率。

需要特别注意的是，RedissonLock 同样没有解决 节点挂掉的时候，存在丢失锁的风险的问题。而现实情况是有一些场景无法容忍的，所以 Redisson 提供了实现了redlock算法的 RedissonRedLock，RedissonRedLock 真正解决了单点失败的问题，代价是需要额外的为 RedissonRedLock 搭建Redis环境。

所以，如果业务场景可以容忍这种小概率的错误，则推荐使用 RedissonLock， 如果无法容忍，则推荐使用 RedissonRedLock。

### 参考

> https://github.com/javazhiyin/advanced-java/
> https://crazyfzw.github.io/2019/04/15/distributed-locks-with-redis/





针对项目中使用的分布式锁进行简单的示例配置以及源码解析，并列举源码中使用到的一些基础知识点，但是没有对redisson中使用到的netty知识进行解析。

本篇主要是对以下几个方面进行了探索

- Maven配置
- RedissonLock简单示例
- 源码中使用到的Redis命令
- 源码中使用到的lua脚本语义
- 源码分析

### **Maven配置**

```
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>2.2.12</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>2.6.0</version>
</dependency>
```

### **RedissonLock简单示例**

redission支持4种连接redis方式，分别为单机、主从、Sentinel、Cluster 集群，项目中使用的连接方式是Sentinel。

redis服务器不在本地的同学请注意权限问题。

#### **Sentinel配置**

```
Config config = new Config();
config.useSentinelServers().addSentinelAddress("127.0.0.1:6479", "127.0.0.1:6489").setMasterName("master").setPassword("password").setDatabase(0);
RedissonClient redisson = Redisson.create(config);
```

#### 简单使用

```
RLock lock = redisson.getLock("test_lock");
try{
    boolean isLock=lock.tryLock();
    if(isLock){
        doBusiness();
    }
}catch(exception e){
}finally{
    lock.unlock();
}
```

### **源码中使用到的Redis命令**

分布式锁主要需要以下redis命令，这里列举一下。在源码分析部分可以继续参照命令的操作含义。

1. EXISTS key :当 key 存在，返回1；若给定的 key 不存在，返回0。
2. GETSET key value:将给定 key 的值设为 value ，并返回 key 的旧值 (old value)，当 key 存在但不是字符串类型时，返回一个错误，当key不存在时，返回nil。
3. GET key:返回 key 所关联的字符串值，如果 key 不存在那么返回 nil。
4. DEL key [KEY …]:删除给定的一个或多个 key ,不存在的 key 会被忽略,返回实际删除的key的个数（integer）。
5. HSET key field value：给一个key 设置一个{field=value}的组合值，如果key没有就直接赋值并返回1，如果field已有，那么就更新value的值，并返回0.
6. HEXISTS key field:当key中存储着field的时候返回1，如果key或者field至少有一个不存在返回0。
7. HINCRBY key field increment:将存储在key中的哈希（Hash）对象中的指定字段field的值加上增量increment。如果键key不存在，一个保存了哈希对象的新建将被创建。如果字段field不存在，在进行当前操作前，其将被创建，且对应的值被置为0，返回值是增量之后的值
8. PEXPIRE key milliseconds：设置存活时间，单位是毫秒。expire操作单位是秒。
9. PUBLISH channel message:向channel post一个message内容的消息，返回接收消息的客户端数。

### **源码中使用到的lua脚本语义**

Redisson源码中，执行redis命令的是lua脚本，其中主要用到如下几个概念。

- redis.call() 是执行redis命令.
- KEYS[1] 是指脚本中第1个参数
- ARGV[1] 是指脚本中第一个参数的值
- 返回值中nil与false同一个意思。

需要注意的是，在redis执行lua脚本时，相当于一个redis级别的锁，不能执行其他操作，类似于原子操作，也是redisson实现的一个关键点。

另外，如果lua脚本执行过程中出现了异常或者redis服务器直接宕掉了，执行redis的根据日志回复的命令，会将脚本中已经执行的命令在日志中删除。

#### **源码分析**

##### RLOCK结构

```
public interface RLock extends Lock, RExpirable {
    void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException;
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;
    void lock(long leaseTime, TimeUnit unit);
    void forceUnlock();
    boolean isLocked();
    boolean isHeldByCurrentThread();
    int getHoldCount();
    Future<Void> unlockAsync();
    Future<Boolean> tryLockAsync();
    Future<Void> lockAsync();
    Future<Void> lockAsync(long leaseTime, TimeUnit unit);
    Future<Boolean> tryLockAsync(long waitTime, TimeUnit unit);
    Future<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit);
}
```

该接口主要继承了Lock接口, 并扩展了部分方法, 比如:boolean tryLock(long waitTime, long leaseTime, TimeUnit unit)新加入的leaseTime主要是用来设置锁的过期时间, 如果超过leaseTime还没有解锁的话, redis就强制解锁. leaseTime的默认时间是30s

#### RedissonLock获取锁 tryLock源码

```
Future<Long> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId) {
       internalLockLeaseTime = unit.toMillis(leaseTime);
       return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_LONG,
                 "if (redis.call('exists', KEYS[1]) == 0) then " +
                     "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                     "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                     "return nil; " +
                 "end; " +
                 "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                     "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                     "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                     "return nil; " +
                 "end; " +
                 "return redis.call('pttl', KEYS[1]);",
                   Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
   }
```

其中：

- KEYS[1] 表示的是 getName() ，代表的是锁名 test_lock
- ARGV[1] 表示的是 internalLockLeaseTime 默认值是30s
- ARGV[2] 表示的是 getLockName(threadId) 代表的是 id:threadId 用锁对象id+线程id， 表示当前访问线程，用于区分不同服务器上的线程。

逐句分析：

```
if (redis.call('exists', KEYS[1]) == 0) then 
         redis.call('hset', KEYS[1], ARGV[2], 1); 
         redis.call('pexpire', KEYS[1], ARGV[1]); 
         return nil;
         end;if (redis.call('exists', KEYS[1]) == 0) then 
         redis.call('hset', KEYS[1], ARGV[2], 1); 
         redis.call('pexpire', KEYS[1], ARGV[1]); 
         return nil;
         end;
```

`if (redis.call(‘exists’, KEYS[1]) == 0)` 如果锁名称不存在

`then redis.call(‘hset’, KEYS[1], ARGV[2],1)` 则向redis中添加一个key为test_lock的set，并且向set中添加一个field为线程id，值=1的键值对，表示此线程的重入次数为1

`redis.call(‘pexpire’, KEYS[1], ARGV[1])` 设置set的过期时间，防止当前服务器出问题后导致死锁，return nil; end;返回nil 结束

```
if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then 
         redis.call('hincrby', KEYS[1], ARGV[2], 1); 
         redis.call('pexpire', KEYS[1], ARGV[1]);
         return nil; 
         end;
```

`if (redis.call(‘hexists’, KEYS[1], ARGV[2]) == 1)` 如果锁是存在的，检测是否是当前线程持有锁，如果是当前线程持有锁

`then redis.call(‘hincrby’, KEYS[1], ARGV[2], 1)`则将该线程重入的次数++

`redis.call(‘pexpire’, KEYS[1], ARGV[1])` 并且重新设置该锁的有效时间

`return nil; end;`返回nil，结束

```
return redis.call('pttl', KEYS[1]);
```

锁存在, 但不是当前线程加的锁，则返回锁的过期时间。

#### RedissonLock解锁 unlock源码

```
@Override
    public void unlock() {
        Boolean opStatus = commandExecutor.evalWrite(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                        "if (redis.call('exists', KEYS[1]) == 0) then " +
                            "redis.call('publish', KEYS[2], ARGV[1]); " +
                            "return 1; " +
                        "end;" +
                        "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                            "return nil;" +
                        "end; " +
                        "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                        "if (counter > 0) then " +
                            "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                            "return 0; " +
                        "else " +
                            "redis.call('del', KEYS[1]); " +
                            "redis.call('publish', KEYS[2], ARGV[1]); " +
                            "return 1; "+
                        "end; " +
                        "return nil;",
                        Arrays.<Object>asList(getName(), getChannelName()), LockPubSub.unlockMessage, internalLockLeaseTime, getLockName(Thread.currentThread().getId()));
        if (opStatus == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by current thread by node id: "
                    + id + " thread-id: " + Thread.currentThread().getId());
        }
        if (opStatus) {
            cancelExpirationRenewal();
        }
    }
```

其中：

- KEYS[1] 表示的是getName() 代表锁名test_lock
- KEYS[2] 表示getChanelName() 表示的是发布订阅过程中使用的Chanel
- ARGV[1] 表示的是LockPubSub.unLockMessage 是解锁消息，实际代表的是数字 0，代表解锁消息
- ARGV[2] 表示的是internalLockLeaseTime 默认的有效时间 30s
- ARGV[3] 表示的是getLockName(thread.currentThread().getId())，是当前锁id+线程id

语义分析:

```
if (redis.call('exists', KEYS[1]) == 0) then
         redis.call('publish', KEYS[2], ARGV[1]);
         return 1;
         end;
```

`if (redis.call(‘exists’, KEYS[1]) == 0)` 如果锁已经不存在(可能是因为过期导致不存在，也可能是因为已经解锁)

`then redis.call(‘publish’, KEYS[2], ARGV[1])` 则发布锁解除的消息

`return 1; end` 返回1结束

```
if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then 
         return nil;
         end;
```

`if (redis.call(‘hexists’, KEYS[1], ARGV[3]) == 0)` 如果锁存在，但是若果当前线程不是加锁的线

`then return nil;end`则直接返回nil 结束

```
local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1);
if (counter > 0) then
         redis.call('pexpire', KEYS[1], ARGV[2]); 
         return 0;
else
         redis.call('del', KEYS[1]);
         redis.call('publish', KEYS[2], ARGV[1]);
         return 1;
end;
```

`local counter = redis.call(‘hincrby’, KEYS[1], ARGV[3], -1)` 如果是锁是当前线程所添加，定义变量counter，表示当前线程的重入次数-1,即直接将重入次数-1

`if (counter > 0)`如果重入次数大于0，表示该线程还有其他任务需要执行

`then redis.call(‘pexpire’, KEYS[1], ARGV[2])` 则重新设置该锁的有效时间

`return 0` 返回0结束

`else redis.call(‘del’, KEYS[1])`否则表示该线程执行结束，删除该锁

`redis.call(‘publish’, KEYS[2], ARGV[1])`并且发布该锁解除的消息

`return 1; end;`返回1结束

```
return nil;
```

其他情况返回nil并结束

```
if (opStatus == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by current thread by node id: "
                    + id + " thread-id: " + Thread.currentThread().getId());
        }
```

脚本执行结束之后，如果返回值不是0或1，即当前线程去解锁其他线程的加锁时，抛出异常。

#### RedissonLock强制解锁源码

```
@Override
    public void forceUnlock() {
        get(forceUnlockAsync());
    }
    Future<Boolean> forceUnlockAsync() {
        cancelExpirationRenewal();
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('del', KEYS[1]) == 1) then "
                + "redis.call('publish', KEYS[2], ARGV[1]); "
                + "return 1 "
                + "else "
                + "return 0 "
                + "end",
                Arrays.<Object>asList(getName(), getChannelName()), LockPubSub.unlockMessage);
    }
```

以上是强制解锁的源码,在源码中并没有找到forceUnlock()被调用的痕迹(也有可能是我没有找对),但是forceUnlockAsync()方法被调用的地方很多，大多都是在清理资源时删除锁。此部分比较简单粗暴，删除锁成功则并发布锁被删除的消息，返回1结束，否则返回0结束。

### **总结**

这里只是简单的一个redisson分布式锁的测试用例，并分析了执行lua脚本这部分，如果要继续分析执行结束之后的操作，需要进行netty源码分析 ，redisson使用了netty完成异步和同步的处理。

## 13.如何正确访问Redis中的海量数据？服务才不会挂掉

### 前言

有时候我们需要知道线上的redis的使用情况，尤其需要知道一些前缀的key值，让我们怎么去查看呢？今天老顾分享一个小知识点

### 事故产生

因为我们的用户token缓存是采用了【user_token:userid】格式的key，保存用户的token的值。我们运维为了帮助开发小伙伴们查一下线上现在有多少登录用户。

直接用了keys user_token*方式进行查询，事故就此发生了。导致redis不可用，假死。

### 分析原因

我们线上的登录用户有几百万，数据量比较多；keys算法是遍历算法，复杂度是O(n)，也就是数据越多，时间复杂度越高。

数据量达到几百万，keys这个指令就会导致 Redis 服务卡顿，因为 Redis 是单线程程序，顺序执行所有指令，其它指令必须等到当前的 keys 指令执行完了才可以继续。

### 解决方案

那我们如何去遍历大数据量呢？这个也是面试经常问的。我们可以采用redis的另一个命令scan。我们看一下scan的特点

- 复杂度虽然也是 O(n)，但是它是通过游标分步进行的，不会阻塞线程
- 提供 count 参数，不是结果数量，是redis单次遍历字典槽位数量(约等于)
- 同 keys 一样，它也提供模式匹配功能;
- 服务器不需要为游标保存状态，游标的唯一状态就是 scan 返回给客户端的游标整数;
- 返回的结果可能会有重复，需要客户端去重复，这点非常重要;
- 单次返回的结果是空的并不意味着遍历结束，而要看返回的游标值是否为零

#### 一、scan命令格式

```
SCAN cursor [MATCH pattern] [COUNT count]
```

#### 二、命令解释：scan 游标 MATCH <返回和给定模式相匹配的元素> count 每次迭代所返回的元素数量

SCAN命令是增量的循环，每次调用只会返回一小部分的元素。所以不会让redis假死

SCAN命令返回的是一个游标，从0开始遍历，到0结束遍历

#### 三、举例

```
redis > scan 0 match user_token* count 5 
 1) "6"
 2) 1) "user_token:1000"
 2) "user_token:1001"
 3) "user_token:1010"
 4) "user_token:2300"
 5) "user_token:1389"
```

从0开始遍历，返回了游标6，又返回了数据，继续scan遍历，就要从6开始

```
redis > scan 6 match user_token* count 5 
 1) "10"
 2) 1) "user_token:3100"
 2) "user_token:1201"
 3) "user_token:1410"
 4) "user_token:5300"
 5) "user_token:3389"
```

### 总结

这个是面试经常会问到的，也是我们小伙伴在工作的过程经常用的，一般小公司，不会有什么问题，但数据量多的时候，你的操作方式不对，你的绩效就会被扣哦，哈哈。谢谢！！！

*本文来源：toutiao.com/i6697540366528152077*

## 14.Redis分布式锁如何解决锁超时问题

### 一、前言

关于redis分布式锁, 查了很多资料, 发现很多只是实现了最基础的功能, 但是, 并没有解决当锁已超时而业务逻辑还未执行完的问题, 这样会导致: A线程超时时间设为10s(为了解决死锁问题), 但代码执行时间可能需要30s, 然后redis服务端10s后将锁删除, 此时, B线程恰好申请锁, redis服务端不存在该锁, 可以申请, 也执行了代码, 那么问题来了, A、B线程都同时获取到锁并执行业务逻辑, 这与分布式锁最基本的性质相违背: 在任意一个时刻, 只有一个客户端持有锁, 即独享

为了解决这个问题, 本文将用完整的代码和测试用例进行验证, 希望能给小伙伴带来一点帮助

### 二、准备工作

压测工具jmeter

> https://pan.baidu.com/share/init?surl=NN0c0tDYQjBTTPA-WTT3yg
> 提取码: 8f2a

redis-desktop-manager客户端

> https://pan.baidu.com/share/init?surl=NoJtZZZOXsk45aQYtveWbQ
> 提取码: 9bhf

postman

> https://pan.baidu.com/share/init?surl=28sGJk4zxoOknAd-47hE7w
> 提取码: vfu7

也可以直接官网下载, 我这边都整理到网盘了

需要postman是因为我还没找到jmeter多开窗口的办法, 哈哈

### 三、说明

1、springmvc项目

2、maven依赖

```
        <!--redis-->
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-redis</artifactId>
            <version>1.6.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>2.7.3</version>
        </dependency>
```

3、核心类

- 分布式锁工具类: DistributedLock
- 测试接口类: PcInformationServiceImpl
- 锁延时守护线程类: PostponeTask

### 四、实现思路

先测试在不开启锁延时线程的情况下, A线程超时时间设为10s, 执行业务逻辑时间设为30s, 10s后, 调用接口, 查看是否能够获取到锁, 如果获取到, 说明存在线程安全性问题

同上, 在加锁的同时, 开启锁延时线程, 调用接口, 查看是否能够获取到锁, 如果获取不到, 说明延时成功, 安全性问题解决

### 五、实现

#### 1、版本01代码

##### 1)、DistributedLock

```
package com.cn.pinliang.common.util;

import com.cn.pinliang.common.thread.PostponeTask;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Collections;

@Component
public class DistributedLock {

    @Autowired
    private RedisTemplate<Serializable, Object> redisTemplate;

    private static final Long RELEASE_SUCCESS = 1L;

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    // 解锁脚本(lua)
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * 分布式锁
     * @param key
     * @param value
     * @param expireTime 单位: 秒
     * @return
     */
    public boolean lock(String key, String value, long expireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            String result = jedis.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
            if (LOCK_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }

    /**
     * 解锁
     * @param key
     * @param value
     * @return
     */
    public Boolean unLock(String key, String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(value));
            if (RELEASE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }

}
```

说明: 就2个方法, 加锁解锁, 加锁使用jedis setnx方法, 解锁执行lua脚本, 都是原子性操作

##### 2)、PcInformationServiceImpl

```
    public JsonResult add() throws Exception {
        String key = "add_information_lock";
        String value = RandomUtil.produceStringAndNumber(10);
        long expireTime = 10L;

        boolean lock = distributedLock.lock(key, value, expireTime);
        String threadName = Thread.currentThread().getName();
        if (lock) {
            System.out.println(threadName + " 获得锁...............................");
            Thread.sleep(30000);
            distributedLock.unLock(key, value);
            System.out.println(threadName + " 解锁了...............................");
        } else {
            System.out.println(threadName + " 未获取到锁...............................");
            return JsonResult.fail("未获取到锁");
        }

        return JsonResult.succeed();
    }
```

说明: 测试类很简单, value随机生成, 保证唯一, 不会在超时情况下解锁其他客户端持有的锁

##### 3)、打开redis-desktop-manager客户端, 刷新缓存, 可以看到, 此时是没有add_information_lock的key的

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZvCiaCPPeiaKAlKbUialjsMrVDv8dgxlLKFIG9ASUicNsepApOBQa7RCN0w/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##### 4)、启动jmeter, 调用接口测试

设置5个线程同时访问, 在10s的超时时间内查看redis, add_information_lock存在, 多次调接口, 只有一个线程能够获取到锁

redis

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZibszSrIQUa2T9LuxUv4rRBfuyTbR9s8MBefN3LtnoZX47OSayys7yGA/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1-4个请求, 都未获取到锁

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZITHAZaGtDeI66WqcZgcTiboSRfkTXYyYr8xqWDiagwSeD1k66HSWAkvg/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

第5个请求, 获取到锁

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZx7mM6fkdk3qN4EOUq4akjRJHLo2w6G2UgAFuJ9pEdcUqw9SEz6aFfw/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

OK, 目前为止, 一切正常, 接下来测试10s之后, A仍在执行业务逻辑, 看别的线程是否能获取到锁

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZ6Xjo9OYll3PGyQyr4jwtO4ynHb2U0ZVY4lQyibMJnpbiaMBkt4rEVW6Q/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)可以看到, 操作成功, 说明A和B同时执行了这段本应该独享的代码, 需要优化。

#### 2、版本02代码

##### 1)、DistributedLock

```
package com.cn.pinliang.common.util;

import com.cn.pinliang.common.thread.PostponeTask;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Collections;

@Component
public class DistributedLock {

    @Autowired
    private RedisTemplate<Serializable, Object> redisTemplate;

    private static final Long RELEASE_SUCCESS = 1L;
    private static final Long POSTPONE_SUCCESS = 1L;

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    // 解锁脚本(lua)
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    // 延时脚本
    private static final String POSTPONE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1], ARGV[2]) else return '0' end";

    /**
     * 分布式锁
     * @param key
     * @param value
     * @param expireTime 单位: 秒
     * @return
     */
    public boolean lock(String key, String value, long expireTime) {
        // 加锁
        Boolean locked = redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            String result = jedis.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
            if (LOCK_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });

        if (locked) {
            // 加锁成功, 启动一个延时线程, 防止业务逻辑未执行完毕就因锁超时而使锁释放
            PostponeTask postponeTask = new PostponeTask(key, value, expireTime, this);
            Thread thread = new Thread(postponeTask);
            thread.setDaemon(Boolean.TRUE);
            thread.start();
        }

        return locked;
    }

    /**
     * 解锁
     * @param key
     * @param value
     * @return
     */
    public Boolean unLock(String key, String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(value));
            if (RELEASE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }

    /**
     * 锁延时
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public Boolean postpone(String key, String value, long expireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(POSTPONE_LOCK_SCRIPT, Lists.newArrayList(key), Lists.newArrayList(value, String.valueOf(expireTime)));
            if (POSTPONE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }

}
```

说明: 新增了锁延时方法, lua脚本, 自行脑补相关语法

##### 2)、PcInformationServiceImpl不需要改动

##### 3)、PostponeTask

```
package com.cn.pinliang.common.thread;

import com.cn.pinliang.common.util.DistributedLock;

public class PostponeTask implements Runnable {

    private String key;
    private String value;
    private long expireTime;
    private boolean isRunning;
    private DistributedLock distributedLock;

    public PostponeTask() {
    }

    public PostponeTask(String key, String value, long expireTime, DistributedLock distributedLock) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
        this.isRunning = Boolean.TRUE;
        this.distributedLock = distributedLock;
    }

    @Override
    public void run() {
        long waitTime = expireTime * 1000 * 2 / 3;// 线程等待多长时间后执行
        while (isRunning) {
            try {
                Thread.sleep(waitTime);
                if (distributedLock.postpone(key, value, expireTime)) {
                    System.out.println("延时成功...........................................................");
                } else {
                    this.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        this.isRunning = Boolean.FALSE;
    }

}
```

说明: 调用lock同时, 立即开启PostponeTask线程, 线程等待超时时间的2/3时间后, 开始执行锁延时代码, 如果延时成功, add_information_lock这个key会一直存在于redis服务端, 直到业务逻辑执行完毕, 因此在此过程中, 其他线程无法获取到锁, 也即保证了线程安全性

下面是测试结果

10s后, 查看redis服务端, add_information_lock仍存在, 说明延时成功

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZzuWbMsicxoqn8gVSG2qicWuRn8avQPNhEuYz2djv9t5Qn1k28yL1HSEQ/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

此时用postman再次请求, 发现获取不到锁

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZkFMc9UibkIXXKAW9k713Z8YiafGVngXQS8iclHbueic3mmRxWzNaU7ahyg/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

看一下控制台打印

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZBYmXAtSXoruWYstS9AKhZXSu66lUn26EecGDu22dCq6cuxqJcibvbxQ/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](https://mmbiz.qpic.cn/mmbiz/8KKrHK5ic6XDYtFdFOKmywXEZibICmNEkZcde96BY88Yf85HsZY3CNz3HkKcsV88okDGiaSFEczKSvErNrQGgegNQ/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

A线程在19:09:11获取到锁, 在10 * 2 / 3 = 6s后进行延时, 成功, 保证了业务逻辑未执行完毕的情况下不会释放锁

A线程执行完毕, 锁释放, 其他线程又可以竞争锁

OK, 目前为止, 解决了锁超时而业务逻辑仍在执行的锁冲突问题, 还很简陋, 而最严谨的方式还是使用官方的 Redlock 算法实现, 其中 Java 包推荐使用 redisson, 思路差不多其实, 都是在快要超时时续期, 以保证业务逻辑未执行完毕不会有其他客户端持有锁

也可以看看redisson相关内容：[Redisson是如何实现分布式锁的？](http://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247493252&idx=2&sn=5530b330af0e0bcb56f9cc8bd7d0a25d&chksm=ebd5d9a8dca250be07d54c37110fcc2549cb31968557910a77485747d9cb9ee842a2f05c25dc&scene=21#wechat_redirect)

## 15.Redis的各项功能解决了哪些问题

### 先看一下Redis是一个什么东西。

> 官方简介解释到：Redis是一个基于BSD开源的项目，是一个把结构化的数据放在内存中的一个存储系统，你可以把它作为数据库，缓存和消息中间件来使用。
>
> 
>
> 同时支持strings，lists，hashes，sets，sorted sets，bitmaps，hyperloglogs和geospatial indexes等数据类型。
>
> 
>
> 它还内建了复制，lua脚本，LRU，事务等功能，通过redis sentinel实现高可用，通过redis cluster实现了自动分片。以及事务，发布/订阅，自动故障转移等等。

综上所述，Redis提供了丰富的功能，初次见到可能会感觉眼花缭乱，这些功能都是干嘛用的？都解决了什么问题？什么情况下才会用到相应的功能？那么下面从零开始，一步一步的演进来粗略的解释下。

### 1 从零开始

最初的需求非常简单，我们有一个提供热点新闻列表的api：http://api.xxx.com/hot-news，api的消费者抱怨说每次请求都要2秒左右才能返回结果。

随后我们就着手于如何提升一下api消费者感知的性能，很快最简单粗暴的第一个方案就出来了：为API的响应加上基于HTTP的缓存控制 cache-control:max-age=600 ，即让消费者可以缓存这个响应十分钟。

如果api消费者如果有效的利用了响应中的缓存控制信息，则可以有效的改善其感知的性能（10分钟以内）。但是还有2个弊端：第一个是在缓存生效的10分钟内，api消费者可能会得到旧的数据；第二个是如果api的客户端无视缓存直接访问API依然是需要2秒，治标不治本呐。

### 2 基于本机内存的缓存

为了解决调用API依然需要2秒的问题，经过排查，其主要原因在于使用SQL获取热点新闻的过程中消耗了将近2秒的时间，于是乎，我们又想到了一个简单粗暴的解决方案，即把SQL查询的结果直接缓存在当前api服务器的内存中（设置缓存有效时间为1分钟）。

后续1分钟内的请求直接读缓存，不再花费2秒去执行SQL了。假如这个api每秒接收到的请求时100个，那么一分钟就是6000个，也就是只有前2秒拥挤过来的请求会耗时2秒，后续的58秒中的所有请求都可以做到即使响应，而无需再等2秒的时间。

其他API的小伙伴发现这是个好办法，于是很快我们就发现API服务器的内存要爆满了。。。

### 3 服务端的Redis

在API服务器的内存都被缓存塞满的时候，我们发现不得不另想解决方案了。最直接的想法就是我们把这些缓存都丢到一个专门的服务器上吧，把它的内存配置的大大的。然后我们就盯上了redis。。。

至于如何配置部署redis这里不解释了，redis官方有详细的介绍。随后我们就用上了一台单独的服务器作为Redis的服务器，API服务器的内存压力得以解决。

#### 3.1 持久化（Persistence）

单台的Redis服务器一个月总有那么几天心情不好，心情不好就罢工了，导致所有的缓存都丢失了（redis的数据是存储在内存的嘛）。虽然可以把Redis服务器重新上线，但是由于内存的数据丢失，造成了缓存雪崩，API服务器和数据库的压力还是一下子就上来了。

所以这个时候Redis的持久化功能就派上用场了，可以缓解一下缓存雪崩带来的影响。redis的持久化指的是redis会把内存的中的数据写入到硬盘中，在redis重新启动的时候加载这些数据，从而最大限度的降低缓存丢失带来的影响。

#### 3.2 哨兵（Sentinel）和复制（Replication）

Redis服务器毫无征兆的罢工是个麻烦事。那么怎办办？答曰：备份一台，你挂了它上。那么如何得知某一台redis服务器挂了，如何切换，如何保证备份的机器是原始服务器的完整备份呢？这时候就需要Sentinel和Replication出场了。

Sentinel可以管理多个Redis服务器，它提供了监控，提醒以及自动的故障转移的功能；Replication则是负责让一个Redis服务器可以配备多个备份的服务器。Redis也是利用这两个功能来保证Redis的高可用的。此外，Sentinel功能则是对Redis的发布和订阅功能的一个利用。

#### 3.3 集群（Cluster）

单台服务器资源的总是有上限的，CPU资源和IO资源我们可以通过主从复制，进行读写分离，把一部分CPU和IO的压力转移到从服务器上。但是内存资源怎么办，主从模式做到的只是相同数据的备份，并不能横向扩充内存；单台机器的内存也只能进行加大处理，但是总有上限的。

所以我们就需要一种解决方案，可以让我们横向扩展。最终的目的既是把每台服务器只负责其中的一部分，让这些所有的服务器构成一个整体，对外界的消费者而言，这一组分布式的服务器就像是一个集中式的服务器一样（之前在解读REST的博客中解释过分布式于基于网络的差异：基于网络应用的架构）。

在Redis官方的分布式方案出来之前，有twemproxy和codis两种方案，这两个方案总体上来说都是依赖proxy来进行分布式的，也就是说redis本身并不关心分布式的事情，而是交由twemproxy和codis来负责。

而redis官方给出的cluster方案则是把分布式的这部分事情做到了每一个redis服务器中，使其不再需要其他的组件就可以独立的完成分布式的要求。我们这里不关心这些方案的优略，我们关注一下这里的分布式到底是要处理那些事情?也就是twemproxy和codis独立处理的处理分布式的这部分逻辑和cluster集成到redis服务的这部分逻辑到底在解决什么问题？

如我们前面所说的，一个分布式的服务在外界看来就像是一个集中式的服务一样。那么要做到这一点就面临着有一个问题需要解决：既是增加或减少分布式服务中的服务器的数量，对消费这个服务的客户端而言应该是无感的；那么也就意味着客户端不能穿透分布式服务，把自己绑死到某一个台的服务器上去，因为一旦如此，你就再也无法新增服务器，也无法进行故障替换。

**解决这个问题有两个路子：**

第一个路子最直接，那就是我加一个中间层来隔离这种具体的依赖，即twemproxy采用的方式，让所有的客户端只能通过它来消费redsi服务，通过它来隔离这种依赖（但是你会发现twermproxy会成为一个单点），这种情况下每台redis服务器都是独立的，它们之间彼此不知对方的存在；

第二个路子是让redis服务器知道彼此的存在，通过重定向的机制来引导客户端来完成自己所需要的操作，比如客户端链接到了某一个redis服务器，说我要执行这个操作，redis服务器发现自己无法完成这个操作，那么就把能完成这个操作的服务器的信息给到客户端，让客户端去请求另外的一个服务器，这时候你就会发现每一个redis服务器都需要保持一份完整的分布式服务器信息的一份资料，不然它怎么知道让客户端去找其他的哪个服务器来执行客户端想要的操作呢。

上面这一大段解释了这么多，不知有没有发现不管是第一个路子还是第二个路子，都有一个共同的东西存在，那就是分布式服务中所有服务器以及其能提供的服务的信息。这些信息无论如何也是要存在的，**区别在于第一个路子是把这部分信息单独来管理，用这些信息来协调后端的多个独立的redis服务器；第二个路子则是让每一个redis服务器都持有这份信息，彼此知道对方的存在，来达成和第一个路子一样的目的，优点是不再需要一个额外的组件来处理这部分事情。**

Redis Cluster的具体实现细节则是采用了Hash槽的概念，即预先分配出来16384个槽：在客户端通过对Key进行CRC16（key）% 16384运算得到对应的槽是哪一个；在redis服务端则是每个服务器负责一部分槽，当有新的服务器加入或者移除的时候，再来迁移这些槽以及其对应的数据，同时每个服务器都持有完整的槽和其对应的服务器的信息，这就使得服务器端可以进行对客户端的请求进行重定向处理。

### 4 客户端的Redis

上面的第三小节主要介绍的是Redis服务端的演进步骤，解释了Redis如何从一个单机的服务，进化为一个高可用的、去中心化的、分布式的存储系统。这一小节则是关注下客户端可以消费的redis服务。

#### 4.1 数据类型

redis支持丰富的数据类型，从最基础的string到复杂的常用到的数据结构都有支持：

- string：最基本的数据类型，二进制安全的字符串，最大512M。
- list：按照添加顺序保持顺序的字符串列表。
- set：无序的字符串集合，不存在重复的元素。
- sorted set：已排序的字符串集合。
- hash：key-value对的一种集合。
- bitmap：更细化的一种操作，以bit为单位。
- hyperloglog：基于概率的数据结构。

这些众多的数据类型，主要是为了支持各种场景的需要，当然每种类型都有不同的时间复杂度。其实这些复杂的数据结构相当于之前我在《解读REST》这个系列博客基于网络应用的架构风格中介绍到的远程数据访问（Remote Data Access = RDA）的具体实现，即通过在服务器上执行一组标准的操作命令，在服务端之间得到想要的缩小后的结果集，从而简化客户端的使用，也可以提高网络性能。比如如果没有list这种数据结构，你就只能把list存成一个string，客户端拿到完整的list，操作后再完整的提交给redis，会产生很大的浪费。

#### 4.2 事务

上述数据类型中，每一个数据类型都有独立的命令来进行操作，很多情况下我们需要一次执行不止一个命令，而且需要其同时成功或者失败。redis对事务的支持也是源自于这部分需求，即支持一次性按顺序执行多个命令的能力，并保证其原子性。

#### 4.3 Lua脚本

在事务的基础上，如果我们需要在服务端一次性的执行更复杂的操作（包含一些逻辑判断），则lua就可以排上用场了（比如在获取某一个缓存的时候，同时延长其过期时间）。redis保证lua脚本的原子性，一定的场景下，是可以代替redis提供的事务相关的命令的。相当于基于网络应用的架构风格中介绍到的远程求值（Remote Evluation = REV）的具体实现。

#### 4.4 管道

因为redis的客户端和服务器的连接时基于TCP的， 默认每次连接都时只能执行一个命令。管道则是允许利用一次连接来处理多条命令，从而可以节省一些tcp连接的开销。管道和事务的差异在于管道是为了节省通信的开销，但是并不会保证原子性。

#### 4.5 分布式锁

官方推荐采用Redlock算法，即使用string类型，加锁的时候给的一个具体的key，然后设置一个随机的值；取消锁的时候用使用lua脚本来先执行获取比较，然后再删除key。具体的命令如下：

```
SET resource_name my_random_value NX PX 30000

if redis.call("get",KEYS[1]) == ARGV[1] then
    return redis.call("del",KEYS[1])
else
    return 0
end
```

### 总结

本篇着重从抽象层面来解释下redis的各项功能以及其存在的目的，而没有关心其具体的细节是什么。从而可以聚焦于其解决的问题，依据抽象层面的概念可以使得我们在特定的场景下选择更合适的方案，而非局限于其技术细节。

以上均是笔者个人的一些理解，如果不当之处，欢迎指正。

### 参考

> Redis 文档：https://github.com/antirez/redis-doc
>
> Redis 简介：https://redis.io/topics/introduction
>
> Redis 持久化（Persistence）：https://redis.io/topics/persistence
>
> Redis 发布/订阅（Pub/Sub）：https://redis.io/topics/pubsub
>
> Redis 哨兵（Sentinel）：https://redis.io/topics/sentinel
>
> Redis 复制（Replication）：https://redis.io/topics/replication
>
> Redis 集群（cluster）：https://redis.io/topics/cluster-tutorial
>
> RedIs 事务（Transaction）：https://redis.io/topics/transactions
>
> Redis 数据类型（data types）：https://redis.io/topics/data-types-intro
>
> Redis 分布式锁：https://redis.io/topics/distlock
>
> Redis 管道（pipelining ）：https://redis.io/topics/pipelining
>
> Redis Lua Script：https://redis.io/commands/eval

*来源：https://www.cnblogs.com/linianhui/*

## 16.说说Redis两种持久化方式的优缺点

### redis两种持久化的方式

1. RDB持久化可以在指定的时间间隔内生成数据集的时间点快照
2. AOF持久化记录服务器执行的所有写操作命令,并在服务器启动时,通过重新执行这些命令来还原数据集,AOF文件中全部以redis协议的格式来保存,新命令会被追加到文件的末尾,redis还可以在后台对AOF文件进行重写,文件的体积不会超出保存数据集状态所需要的实际大小,
3. redis还可以同时使用AOF持久化和RDB持久化,在这种情况下,当redis重启时,它会有限使用AOF文件来还原数据集,因为AOF文件保存的数据集通常比RDB文件所保存的数据集更加完

### ＲＤＢ的优点

1. RDB 是一个非常紧凑（compact）的文件，它保存了 Redis 在某个时间点上的数据集。这种文件非常适合用于进行备份：比如说，你可以在最近的 24 小时内，每小时备份一次 RDB 文件，并且在每个月的每一天，也备份一个 RDB 文件。这样的话，即使遇上问题，也可以随时将数据集还原到不同的版本。
2. RDB 非常适用于灾难恢复（disaster recovery）：它只有一个文件，并且内容都非常紧凑，可以（在加密后）将它传送到别的数据中心，或者亚马逊 S3 中。
3. RDB 可以最大化 Redis 的性能：父进程在保存 RDB 文件时唯一要做的就是 fork 出一个子进程，然后这个子进程就会处理接下来的所有保存工作，父进程无须执行任何磁盘 I/O 操作。
4. RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快

### ＲＤＢ的缺点

1. 如果你需要尽量避免在服务器故障时丢失数据，那么 RDB 不适合你。虽然 Redis 允许你设置不同的保存点（save point）来控制保存 RDB 文件的频率， 但是， 因为RDB 文件需要保存整个数据集的状态， 所以它并不是一个轻松的操作。因此你可能会至少 5 分钟才保存一次 RDB 文件。在这种情况下， 一旦发生故障停机， 你就可能会丢失好几分钟的数据。
2. 每次保存 RDB 的时候，Redis 都要 fork() 出一个子进程，并由子进程来进行实际的持久化工作。在数据集比较庞大时， fork()可能会非常耗时，造成服务器在某某毫秒内停止处理客户端；如果数据集非常巨大，并且 CPU 时间非常紧张的话，那么这种停止时间甚至可能会长达整整一秒。虽然 AOF 重写也需要进行 fork() ，但无论 AOF 重写的执行间隔有多长，数据的耐久性都不会有任何损失。

### AOF 的优点

1. 使用 AOF 持久化会让 Redis 变得非常耐久（much more durable）：你可以设置不同的 fsync 策略，比如无 fsync ，每秒钟一次 fsync ，或者每次执行写入命令时 fsync 。AOF 的默认策略为每秒钟 fsync 一次，在这种配置下，Redis 仍然可以保持良好的性能，并且就算发生故障停机，也最多只会丢失一秒钟的数据（ fsync 会在后台线程执行，所以主线程可以继续努力地处理命令请求）。
2. AOF 文件是一个只进行追加操作的日志文件（append only log）， 因此对 AOF 文件的写入不需要进行 seek ， 即使日志因为某些原因而包含了未写入完整的命令（比如写入时磁盘已满，写入中途停机，等等）， redis-check-aof 工具也可以轻易地修复这种问题。
3. Redis 可以在 AOF 文件体积变得过大时，自动地在后台对 AOF 进行重写：重写后的新 AOF 文件包含了恢复当前数据集所需的最小命令集合。整个重写操作是绝对安全的，因为 Redis 在创建新 AOF 文件的过程中，会继续将命令追加到现有的 AOF 文件里面，即使重写过程中发生停机，现有的 AOF 文件也不会丢失。而一旦新 AOF 文件创建完毕，Redis 就会从旧 AOF 文件切换到新 AOF 文件，并开始对新 AOF 文件进行追加操作。
4. AOF 文件有序地保存了对数据库执行的所有写入操作， 这些写入操作以 Redis 协议的格式保存， 因此 AOF 文件的内容非常容易被人读懂， 对文件进行分析（parse）也很轻松。导出（export） AOF 文件也非常简单：举个例子， 如果你不小心执行了 FLUSHALL 命令， 但只要 AOF 文件未被重写， 那么只要停止服务器， 移除 AOF 文件末尾的 FLUSHALL 命令， 并重启 Redis ， 就可以将数据集恢复到 FLUSHALL 执行之前的状态。

### AOF 的缺点

1. 对于相同的数据集来说，AOF 文件的体积通常要大于 RDB 文件的体积。
2. 根据所使用的 fsync 策略，AOF 的速度可能会慢于 RDB 。在一般情况下， 每秒 fsync 的性能依然非常高， 而关闭 fsync 可以让 AOF 的速度和 RDB 一样快， 即使在高负荷之下也是如此。不过在处理巨大的写入载入时，RDB 可以提供更有保证的最大延迟时间（latency）。
3. AOF 在过去曾经发生过这样的 bug ：因为个别命令的原因，导致 AOF 文件在重新载入时，无法将数据集恢复成保存时的原样。（举个例子，阻塞命令 BRPOPLPUSH 就曾经引起过这样的 bug 。） 测试套件里为这种情况添加了测试：它们会自动生成随机的、复杂的数据集， 并通过重新载入这些数据来确保一切正常。虽然这种 bug 在 AOF 文件中并不常见， 但是对比来说， RDB 几乎是不可能出现这种 bug 的

*来源：cnblogs.com/ssssdy/p/7132856.html*

## 17.一些Redis面试题及分布式集群面试考点整理

### 1. 使用Redis有哪些好处？

(1) 速度快，因为数据存在内存中，类似于HashMap，HashMap的优势就是查找和操作的时间复杂度都是O(1)

(2) 支持丰富数据类型，支持string，list，set，sorted set，hash

(3) 支持事务，操作都是原子性，所谓的原子性就是对数据的更改要么全部执行，要么全部不执行

(4) 丰富的特性：可用于缓存，消息，按key设置过期时间，过期后将会自动删除

### 2. redis相比memcached有哪些优势？

(1) memcached所有的值均是简单的字符串，redis作为其替代者，支持更为丰富的数据类型

(2) redis的速度比memcached快很多

(3) redis可以持久化其数据

### 3. MySQL里有1000w数据，redis中只存10w的数据，如何保证redis中的数据都是热点数据

相关知识：redis 内存数据集大小上升到一定大小的时候，就会施行数据淘汰策略。redis 提供 6种数据淘汰策略：

- voltile-lru：从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰
- volatile-ttl：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰
- volatile-random：从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰
- allkeys-lru：从数据集（server.db[i].dict）中挑选最近最少使用的数据淘汰
- allkeys-random：从数据集（server.db[i].dict）中任意选择数据淘汰
- no-enviction（驱逐）：禁止驱逐数据

### 4. Memcache与Redis的区别都有哪些？

#### 1)、存储方式

Memecache把数据全部存在内存之中，断电后会挂掉，数据不能超过内存大小。

Redis有部份存在硬盘上，这样能保证数据的持久性。

#### 2)、数据支持类型

Memcache对数据类型支持相对简单。

Redis有复杂的数据类型。

#### 3)、使用底层模型不同

它们之间底层实现方式 以及与客户端之间通信的应用协议不一样。

Redis直接自己构建了VM 机制 ，因为一般的系统调用系统函数的话，会浪费一定的时间去移动和请求。

#### 4）、value大小

redis最大可以达到1GB，而memcache只有1MB

### 5. Redis的各项功能解决了哪些问题？

参考：[【150期】面试官：Redis的各项功能解决了哪些问题？](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247485341&idx=1&sn=4bfe7ba1c575a7f1973bf526c7a270d9&chksm=e80db1ebdf7a38fdaab66d9c94682f8dde49dfb7b0dbe8660308a24bb8dfe8b68bd122ee80d6&scene=21#wechat_redirect)

### 6. redis 两种持久化的优缺点

[【159期】面试官：你来说说Redis两种持久化方式的优缺点](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247485466&idx=1&sn=8fcebf43d54516b8dba4cc676a1162c7&chksm=e80dbe6cdf7a377a5ac8def5bf229fa38d07c995979d66fcb9693a00737274a6306c6b6cf735&scene=21#wechat_redirect)

### 7. redis 线上连接超时处理思路

[【95期】面试官：你遇到 Redis 线上连接超时一般如何处理？](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247484462&idx=1&sn=915ad7fba59fcc7f375a19208d71c9db&chksm=e80db258df7a3b4ef42b6d612233de96fa5b76c7b7e988cbdf5659ffc35e3fa7db7da2682163&scene=21#wechat_redirect)

### 7. 如何对Redis内存进行优化

[【94期】面试官：熟悉Redis吗，项目中你是如何对Redis内存进行优化的](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247484460&idx=1&sn=fbe1377d2e51451311aa910c92de022a&chksm=e80db25adf7a3b4c9d3b38c5c3c73e6ce97dbbcf8c8249acddc452352bf771f28a5ad82c02b1&scene=21#wechat_redirect)

### 高可用分布式集群 

#### 一、什么是分布式

要理解分布式系统，主要需要明白一下2个方面：

- 分布式系统一定是由多个节点组成的系统。

其中，节点指的是计算机服务器，而且这些节点一般不是孤立的，而是互通的。

- 这些连通的节点上部署了我们的节点，并且相互的操作会有协同。

分布式系统对于用户而言，他们面对的就是一个服务器，提供用户需要的服务而已。而实际上这些服务是通过背后的众多服务器组成的一个分布式系统。因此分布式系统看起来像是一个超级计算机一样。

例如淘宝，平时大家都会使用，它本身就是一个分布式系统。我们通过浏览器访问淘宝网站时，这个请求的背后就是一个庞大的分布式系统在为我们提供服务，整个系统中有的负责请求处理，有的负责存储，有的负责计算，最终他们相互协调把最后的结果返回并呈现给用户。

##### 使用分布式系统主要有特点：

1、增大系统容量。我们的业务量越来越大，而要能应对越来越大的业务量，一台机器的性能已经无法满足了，我们需要多台机器才能应对大规模的应用场景。所以，我们需要垂直或是水平拆分业务系统，让其变成一个分布式的架构。

2、加强系统可用。我们的业务越来越关键，需要提高整个系统架构的可用性，这就意味着架构中不能存在单点故障。这样，整个系统不会因为一台机器出故障而导致整体不可用。所以，需要通过分布式架构来冗余系统以消除单点故障，从而提高系统的可用性。

3、因为模块化，所以系统模块重用度更高。

4、因为软件服务模块被拆分，开发和发布速度可以并行而变得更快。

5、系统扩展性更高。

6、团队协作流程也会得到改善。

##### 分布式系统的类型有三种：

1、分布式处理，但只有一个总数据库，没有局部数据库。

2、分层式处理，每一层都有自己的数据库。

3、充分分散的分布式网络，没有中央控制部分，各节点之间的联系方式又可以有多种，如松散的联接，紧密的联接，动态的联接，广播通知式的联接等。

#### 二、高可用

高可用（High Availability），是当一台服务器停止服务后，对于业务及用户毫无影响。停止服务的原因可能由于网卡、路由器、机房、CPU负载过高、内存溢出、自然灾害等不可预期的原因导致，在很多时候也称单点问题。

##### （1）解决单点问题主要有2种方式：

**主备方式**

这种通常是一台主机、一台或多台备机，在正常情况下主机对外提供服务，并把数据同步到备机，当主机宕机后，备机立刻开始服务。

Redis HA中使用比较多的是keepalived，它使主机备机对外提供同一个虚拟IP，客户端通过虚拟IP进行数据操作，正常期间主机一直对外提供服务，宕机后VIP自动漂移到备机上。

优点是对客户端毫无影响，仍然通过VIP操作。

缺点也很明显，在绝大多数时间内备机是一直没使用，被浪费着的。

**主从方式**

这种采取一主多从的办法，主从之间进行数据同步。当Master宕机后，通过选举算法(Paxos、Raft)从slave中选举出新Master继续对外提供服务，主机恢复后以slave的身份重新加入。

主从另一个目的是进行读写分离，这是当单机读写压力过高的一种通用型解决方案。其主机的角色只提供写操作或少量的读，把多余读请求通过负载均衡算法分流到单个或多个slave服务器上。

缺点是主机宕机后，Slave虽然被选举成新Master了，但对外提供的IP服务地址却发生变化了，意味着会影响到客户端。解决这种情况需要一些额外的工作，在当主机地址发生变化后及时通知到客户端，客户端收到新地址后，使用新地址继续发送新请求。

##### （2）数据同步

无论是主备还是主从都牵扯到数据同步的问题，这也分2种情况：

**同步方式：** 当主机收到客户端写操作后，以同步方式把数据同步到从机上，当从机也成功写入后，主机才返回给客户端成功，也称数据强一致性。很显然这种方式性能会降低不少，当从机很多时，可以不用每台都同步，主机同步某一台从机后，从机再把数据分发同步到其他从机上，这样提高主机性能分担同步压力。在redis中是支持这杨配置的，一台master，一台slave，同时这台salve又作为其他slave的master。

**异步方式：** 主机接收到写操作后，直接返回成功，然后在后台用异步方式把数据同步到从机上。这种同步性能比较好，但无法保证数据的完整性，比如在异步同步过程中主机突然宕机了，也称这种方式为数据弱一致性。

Redis主从同步采用的是异步方式，因此会有少量丢数据的危险。还有种弱一致性的特例叫最终一致性，这块详细内容可参见CAP原理及一致性模型。

##### （3）方案选择

keepalived方案配置简单、人力成本小，在数据量少、压力小的情况下推荐使用。如果数据量比较大，不希望过多浪费机器，还希望在宕机后，做一些自定义的措施，比如报警、记日志、数据迁移等操作，推荐使用主从方式，因为和主从搭配的一般还有个管理监控中心。

宕机通知这块，可以集成到客户端组件上，也可单独抽离出来。Redis官方Sentinel支持故障自动转移、通知等，详情见低成本高可用方案设计(四)。

逻辑图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XAuGrbOTt3qCbibAHgmlOzCicVo46X4ibloFhe28cian7ZCCoeLIGG4cTdkPAj6Jyql4rctZbTyPGFXOw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 三，Redis分布式锁如何解决锁超时问题

[【110期】面试官：Redis分布式锁如何解决锁超时问题？](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247484653&idx=1&sn=84301ae46399badec149c487fa8aca6f&chksm=e80db29bdf7a3b8d06f71da980ae85a052546659d25020fa78a7f54b183af56a403df3b190cb&scene=21#wechat_redirect)

#### 四，Redis实现分布式锁的几种常见方式

[【07期】Redis中是如何实现分布式锁的？](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247483934&idx=1&sn=8b5a9dcf5f7601971464bf5e0c9944de&chksm=e80db468df7a3d7eef45c8e35e60c4aae5fa3ffa646dbec3833d5b882506fcef6336c2cac354&scene=21#wechat_redirect)

## 18.分布式锁用 Redis 还是 Zookeeper

### 为什么用分布式锁？

在讨论这个问题之前，我们先来看一个业务场景：

系统A是一个电商系统，目前是一台机器部署，系统中有一个用户下订单的接口，但是用户下订单之前一定要去检查一下库存，确保库存足够了才会给用户下单。

由于系统有一定的并发，所以会预先将商品的库存保存在redis中，用户下单的时候会更新redis的库存。

此时系统架构如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtA3QWLj02ROzbS9Nc3Ws5nCNAXS0RoKfvGXzibXwHAcUSPrrQbouSnI9g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

但是这样一来会**产生一个问题**：假如某个时刻，redis里面的某个商品库存为1，此时两个请求同时到来，其中一个请求执行到上图的第3步，更新数据库的库存为0，但是第4步还没有执行。

而另外一个请求执行到了第2步，发现库存还是1，就继续执行第3步。

这样的结果，是导致卖出了2个商品，然而其实库存只有1个。

很明显不对啊！这就是典型的**库存超卖问题**

此时，我们很容易想到解决方案：用锁把2、3、4步锁住，让他们执行完之后，另一个线程才能进来执行第2步。

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtAOxHO6gxicqjzJAcGEVLiaibiafgnibsXbScFI9FewYrk20USwQ7HvaDjyWQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

按照上面的图，在执行第2步时，使用Java提供的synchronized或者ReentrantLock来锁住，然后在第4步执行完之后才释放锁。

这样一来，2、3、4 这3个步骤就被“锁”住了，多个线程之间只能串行化执行。

但是好景不长，整个系统的并发飙升，一台机器扛不住了。现在要增加一台机器，如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtA5AxlRwFCcEXicQqThGCeTmdWvybbFoSRJ4XFEWIkCAtEalQmjYWDQWg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



增加机器之后，系统变成上图所示，我的天！

假设此时两个用户的请求同时到来，但是落在了不同的机器上，那么这两个请求是可以同时执行了，还是会出现**库存超卖**的问题。

为什么呢？因为上图中的两个A系统，运行在两个不同的JVM里面，他们加的锁只对属于自己JVM里面的线程有效，对于其他JVM的线程是无效的。

因此，这里的问题是：Java提供的原生锁机制在多机部署场景下失效了

这是因为两台机器加的锁不是同一个锁(两个锁在不同的JVM里面)。

那么，我们只要保证两台机器加的锁是同一个锁，问题不就解决了吗？

此时，就该**分布式锁**隆重登场了，分布式锁的思路是：

在整个系统提供一个**全局、唯一**的获取锁的“东西”，然后每个系统在需要加锁时，都去问这个“东西”拿到一把锁，这样不同的系统拿到的就可以认为是同一把锁。

至于这个“东西”，可以是Redis、Zookeeper，也可以是数据库。

文字描述不太直观，我们来看下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtAgiahFdoIdoGMnSeQ3HZ07t7gjRnoErOSeAweBYPDVpbzk3mSzz8qAIw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

通过上面的分析，我们知道了库存超卖场景在分布式部署系统的情况下使用Java原生的锁机制无法保证线程安全，所以我们需要用到分布式锁的方案。

那么，如何实现分布式锁呢？接着往下看！

### 基于Redis实现分布式锁

上面分析为啥要使用分布式锁了，这里我们来具体看看分布式锁落地的时候应该怎么样处理。扩展：[Redisson是如何实现分布式锁的？](http://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247493252&idx=2&sn=5530b330af0e0bcb56f9cc8bd7d0a25d&chksm=ebd5d9a8dca250be07d54c37110fcc2549cb31968557910a77485747d9cb9ee842a2f05c25dc&scene=21#wechat_redirect)



最常见的一种方案就是使用Redis做分布式锁

使用Redis做分布式锁的思路大概是这样的：在redis中设置一个值表示加了锁，然后释放锁的时候就把这个key删除。

具体代码是这样的：

```
// 获取锁
// NX是指如果key不存在就成功，key存在返回false，PX可以指定过期时间
SET anyLock unique_value NX PX 30000


// 释放锁：通过执行一段lua脚本
// 释放锁涉及到两条指令，这两条指令不是原子性的
// 需要用到redis的lua脚本支持特性，redis执行lua脚本是原子性的
if redis.call("get",KEYS[1]) == ARGV[1] then
return redis.call("del",KEYS[1])
else
return 0
end
```



这种方式有几大要点：

- **一定要用SET key value NX PX milliseconds 命令**

  如果不用，先设置了值，再设置过期时间，这个不是原子性操作，有可能在设置过期时间之前宕机，会造成死锁(key永久存在)

- **value要具有唯一性**

  这个是为了在解锁的时候，需要验证value是和加锁的一致才删除key。

  这是避免了一种情况：假设A获取了锁，过期时间30s，此时35s之后，锁已经自动释放了，A去释放锁，但是此时可能B获取了锁。A客户端就不能删除B的锁了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtAnWRNz8QiaFtMLm1xh2MWF8DkpKln4EQ09FnlVIazjibicHiayKNc5hDn3w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

除了要考虑客户端要怎么实现分布式锁之外，还需要考虑redis的部署问题。

redis有3种部署方式：

- 单机模式
- master-slave + sentinel选举模式
- redis cluster模式



使用redis做分布式锁的缺点在于：如果采用单机部署模式，会存在单点问题，只要redis故障了。加锁就不行了。

采用master-slave模式，加锁的时候只对一个节点加锁，即便通过sentinel做了高可用，但是如果master节点故障了，发生主从切换，此时就会有可能出现锁丢失的问题。

基于以上的考虑，其实redis的作者也考虑到这个问题，他提出了一个RedLock的算法，这个算法的意思大概是这样的：

假设redis的部署模式是redis cluster，总共有5个master节点，通过以下步骤获取一把锁：

- 获取当前时间戳，单位是毫秒
- 轮流尝试在每个master节点上创建锁，过期时间设置较短，一般就几十毫秒
- 尝试在大多数节点上建立一个锁，比如5个节点就要求是3个节点（n / 2 +1）
- 客户端计算建立好锁的时间，如果建立锁的时间小于超时时间，就算建立成功了
- 要是锁建立失败了，那么就依次删除这个锁
- 只要别人建立了一把分布式锁，你就得不断轮询去尝试获取锁



但是这样的这种算法还是颇具争议的，可能还会存在不少的问题，无法保证加锁的过程一定正确。

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtAdD3addxvq6PkFVMVyetNiauok1sVIkbicJicLdQbEapbHtcpQAIlr0zsQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 另一种方式：Redisson

此外，实现Redis的分布式锁，除了自己基于redis client原生api来实现之外，还可以使用开源框架：Redission

Redisson是一个企业级的开源Redis Client，也提供了分布式锁的支持。我也非常推荐大家使用，为什么呢？

回想一下上面说的，如果自己写代码来通过redis设置一个值，是通过下面这个命令设置的。

- SET anyLock unique_value NX PX 30000

这里设置的超时时间是30s，假如我超过30s都还没有完成业务逻辑的情况下，key会过期，其他线程有可能会获取到锁。

这样一来的话，第一个线程还没执行完业务逻辑，第二个线程进来了也会出现线程安全问题。所以我们还需要额外的去维护这个过期时间，太麻烦了~

我们来看看redisson是怎么实现的？先感受一下使用redission的爽：

```
Config config = new Config();
config.useClusterServers()
.addNodeAddress("redis://192.168.31.101:7001")
.addNodeAddress("redis://192.168.31.101:7002")
.addNodeAddress("redis://192.168.31.101:7003")
.addNodeAddress("redis://192.168.31.102:7001")
.addNodeAddress("redis://192.168.31.102:7002")
.addNodeAddress("redis://192.168.31.102:7003");

RedissonClient redisson = Redisson.create(config);


RLock lock = redisson.getLock("anyLock");
lock.lock();
lock.unlock();
```



就是这么简单，我们只需要通过它的api中的lock和unlock即可完成分布式锁，他帮我们考虑了很多细节：

- redisson所有指令都通过lua脚本执行，redis支持lua脚本原子性执行

- redisson设置一个key的默认过期时间为30s,如果某个客户端持有一个锁超过了30s怎么办？

  redisson中有一个`watchdog`的概念，翻译过来就是看门狗，它会在你获取锁之后，每隔10秒帮你把key的超时时间设为30s

  这样的话，就算一直持有锁也不会出现key过期了，其他线程获取到锁的问题了。

- redisson的“看门狗”逻辑保证了没有死锁发生。

  (如果机器宕机了，看门狗也就没了。此时就不会延长key的过期时间，到了30s之后就会自动过期了，其他线程可以获取到锁)

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLZO3ZKMzm3Vmc5L6icEx7JtAOWsQamwIB57WoXtHuuDCmpkRRibWOaAdQ6Z0esIicQyvnYBZbFcUic0Cg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这里稍微贴出来其实现代码：

```
// 加锁逻辑
private <T> RFuture<Long> tryAcquireAsync(long leaseTime, TimeUnit unit, final long threadId) {
    if (leaseTime != -1) {
        return tryLockInnerAsync(leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
    }
    // 调用一段lua脚本，设置一些key、过期时间
    RFuture<Long> ttlRemainingFuture = tryLockInnerAsync(commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout(), TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);
    ttlRemainingFuture.addListener(new FutureListener<Long>() {
        @Override
        public void operationComplete(Future<Long> future) throws Exception {
            if (!future.isSuccess()) {
                return;
            }

            Long ttlRemaining = future.getNow();
            // lock acquired
            if (ttlRemaining == null) {
                // 看门狗逻辑
                scheduleExpirationRenewal(threadId);
            }
        }
    });
    return ttlRemainingFuture;
}


<T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
    internalLockLeaseTime = unit.toMillis(leaseTime);

    return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, command,
              "if (redis.call('exists', KEYS[1]) == 0) then " +
                  "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                  "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                  "return nil; " +
              "end; " +
              "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                  "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                  "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                  "return nil; " +
              "end; " +
              "return redis.call('pttl', KEYS[1]);",
                Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
}



// 看门狗最终会调用了这里
private void scheduleExpirationRenewal(final long threadId) {
    if (expirationRenewalMap.containsKey(getEntryName())) {
        return;
    }

    // 这个任务会延迟10s执行
    Timeout task = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
        @Override
        public void run(Timeout timeout) throws Exception {

            // 这个操作会将key的过期时间重新设置为30s
            RFuture<Boolean> future = renewExpirationAsync(threadId);

            future.addListener(new FutureListener<Boolean>() {
                @Override
                public void operationComplete(Future<Boolean> future) throws Exception {
                    expirationRenewalMap.remove(getEntryName());
                    if (!future.isSuccess()) {
                        log.error("Can't update lock " + getName() + " expiration", future.cause());
                        return;
                    }

                    if (future.getNow()) {
                        // reschedule itself
                        // 通过递归调用本方法，无限循环延长过期时间
                        scheduleExpirationRenewal(threadId);
                    }
                }
            });
        }

    }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);

    if (expirationRenewalMap.putIfAbsent(getEntryName(), new ExpirationEntry(threadId, task)) != null) {
        task.cancel();
    }
}
```



另外，redisson还提供了对redlock算法的支持,

它的用法也很简单：

```

RedissonClient redisson = Redisson.create(config);
RLock lock1 = redisson.getFairLock("lock1");
RLock lock2 = redisson.getFairLock("lock2");
RLock lock3 = redisson.getFairLock("lock3");
RedissonRedLock multiLock = new RedissonRedLock(lock1, lock2, lock3);
multiLock.lock();
multiLock.unlock();
```



**小结：**



本节分析了使用redis作为分布式锁的具体落地方案

以及其一些局限性

然后介绍了一个redis的客户端框架redisson，

这也是我推荐大家使用的，

比自己写代码实现会少care很多细节。

### 基于zookeeper实现分布式锁



常见的分布式锁实现方案里面，除了使用redis来实现之外，使用zookeeper也可以实现分布式锁。

在介绍zookeeper(下文用zk代替)实现分布式锁的机制之前，先粗略介绍一下zk是什么东西：

Zookeeper是一种提供配置管理、分布式协同以及命名的中心化服务。

zk的模型是这样的：zk包含一系列的节点，叫做znode，就好像文件系统一样每个znode表示一个目录，然后znode有一些特性：

- **有序节点**：假如当前有一个父节点为`/lock`，我们可以在这个父节点下面创建子节点；

  zookeeper提供了一个可选的有序特性，例如我们可以创建子节点“/lock/node-”并且指明有序，那么zookeeper在生成子节点时会根据当前的子节点数量自动添加整数序号

  也就是说，如果是第一个创建的子节点，那么生成的子节点为`/lock/node-0000000000`，下一个节点则为`/lock/node-0000000001`，依次类推。

  

- **临时节点**：客户端可以建立一个临时节点，在会话结束或者会话超时后，zookeeper会自动删除该节点。

- **事件监听**：在读取数据时，我们可以同时对节点设置事件监听，当节点数据或结构变化时，zookeeper会通知客户端。当前zookeeper有如下四种事件：

- - 节点创建
  - 节点删除
  - 节点数据修改
  - 子节点变更

基于以上的一些zk的特性，我们很容易得出使用zk实现分布式锁的落地方案：

1. 使用zk的临时节点和有序节点，每个线程获取锁就是在zk创建一个临时有序的节点，比如在/lock/目录下。

2. 创建节点成功后，获取/lock目录下的所有临时节点，再判断当前线程创建的节点是否是所有的节点的序号最小的节点

3. 如果当前线程创建的节点是所有节点序号最小的节点，则认为获取锁成功。

4. 如果当前线程创建的节点不是所有节点序号最小的节点，则对节点序号的前一个节点添加一个事件监听。

   比如当前线程获取到的节点序号为`/lock/003`,然后所有的节点列表为`[/lock/001,/lock/002,/lock/003]`,则对`/lock/002`这个节点添加一个事件监听器。



如果锁释放了，会唤醒下一个序号的节点，然后重新执行第3步，判断是否自己的节点序号是最小。

比如`/lock/001`释放了，`/lock/002`监听到时间，此时节点集合为`[/lock/002,/lock/003]`,则`/lock/002`为最小序号节点，获取到锁。



整个过程如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/1J6IbIcPCLblQkicuWPYZicf1yqrpficlt2bhIqLu3VOmTM6qIyibrPc87X2dAoNibxOJ03vtJiaMKfKm0jic7l2rcSng/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

具体的实现思路就是这样，至于代码怎么写，这里比较复杂就不贴出来了。



#### Curator介绍

Curator是一个zookeeper的开源客户端，也提供了分布式锁的实现。

他的使用方式也比较简单：

```
InterProcessMutex interProcessMutex = new InterProcessMutex(client,"/anyLock");
interProcessMutex.acquire();
interProcessMutex.release();
```



其实现分布式锁的核心源码如下：



```
private boolean internalLockLoop(long startMillis, Long millisToWait, String ourPath) throws Exception
{
    boolean  haveTheLock = false;
    boolean  doDelete = false;
    try {
        if ( revocable.get() != null ) {
            client.getData().usingWatcher(revocableWatcher).forPath(ourPath);
        }

        while ( (client.getState() == CuratorFrameworkState.STARTED) && !haveTheLock ) {
            // 获取当前所有节点排序后的集合
            List<String>        children = getSortedChildren();
            // 获取当前节点的名称
            String              sequenceNodeName = ourPath.substring(basePath.length() + 1); // +1 to include the slash
            // 判断当前节点是否是最小的节点
            PredicateResults    predicateResults = driver.getsTheLock(client, children, sequenceNodeName, maxLeases);
            if ( predicateResults.getsTheLock() ) {
                // 获取到锁
                haveTheLock = true;
            } else {
                // 没获取到锁，对当前节点的上一个节点注册一个监听器
                String  previousSequencePath = basePath + "/" + predicateResults.getPathToWatch();
                synchronized(this){
                    Stat stat = client.checkExists().usingWatcher(watcher).forPath(previousSequencePath);
                    if ( stat != null ){
                        if ( millisToWait != null ){
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if ( millisToWait <= 0 ){
                                doDelete = true;    // timed out - delete our node
                                break;
                            }
                            wait(millisToWait);
                        }else{
                            wait();
                        }
                    }
                }
                // else it may have been deleted (i.e. lock released). Try to acquire again
            }
        }
    }
    catch ( Exception e ) {
        doDelete = true;
        throw e;
    } finally{
        if ( doDelete ){
            deleteOurPath(ourPath);
        }
    }
    return haveTheLock;
}
```



其实curator实现分布式锁的底层原理和上面分析的是差不多的。这里我们用一张图详细描述其原理：

![图片](https://mmbiz.qpic.cn/mmbiz_png/1J6IbIcPCLblQkicuWPYZicf1yqrpficlt2wS94DKiaQBjAe4jG6BGrLFmNFqOAWzO5y64Ao3NrKtMQGnTALJ7IKcQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

小结：

本节介绍了zookeeperr实现分布式锁的方案以及zk的开源客户端的基本使用，简要的介绍了其实现原理。相关可以参考：[肝一下ZooKeeper实现分布式锁的方案，附带实例！](http://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247493195&idx=1&sn=84e2caa2f5364db02bfb82a8d9f421e5&chksm=ebd5d967dca250710d905dc9c69529e54d7221984b5c6036c742198c168917a44b95f9dc475b&scene=21#wechat_redirect)

### 两种方案的优缺点比较

学完了两种分布式锁的实现方案之后，本节需要讨论的是redis和zk的实现方案中各自的优缺点。

对于redis的分布式锁而言，它有以下缺点：

- 它获取锁的方式简单粗暴，获取不到锁直接不断尝试获取锁，比较消耗性能。
- 另外来说的话，redis的设计定位决定了它的数据并不是强一致性的，在某些极端情况下，可能会出现问题。锁的模型不够健壮
- 即便使用redlock算法来实现，在某些复杂场景下，也无法保证其实现100%没有问题，关于redlock的讨论可以看How to do distributed locking
- redis分布式锁，其实需要自己不断去尝试获取锁，比较消耗性能。



但是另一方面使用redis实现分布式锁在很多企业中非常常见，而且大部分情况下都不会遇到所谓的“极端复杂场景”

所以使用redis作为分布式锁也不失为一种好的方案，最重要的一点是redis的性能很高，可以支撑高并发的获取、释放锁操作。



对于zk分布式锁而言:

- zookeeper天生设计定位就是分布式协调，强一致性。锁的模型健壮、简单易用、适合做分布式锁。
- 如果获取不到锁，只需要添加一个监听器就可以了，不用一直轮询，性能消耗较小。



但是zk也有其缺点：如果有较多的客户端频繁的申请加锁、释放锁，对于zk集群的压力会比较大。



**小结：**

综上所述，redis和zookeeper都有其优缺点。我们在做技术选型的时候可以根据这些问题作为参考因素。

### 建议

通过前面的分析，实现分布式锁的两种常见方案：redis和zookeeper，他们各有千秋。应该如何选型呢？

就个人而言的话，**我比较推崇zk实现的锁：**

因为redis是有可能存在隐患的，可能会导致数据不对的情况。但是，怎么选用要看具体在公司的场景了。

如果公司里面有zk集群条件，优先选用zk实现，但是如果说公司里面只有redis集群，没有条件搭建zk集群。

那么其实用redis来实现也可以，另外还可能是系统设计者考虑到了系统已经有redis，但是又不希望再次引入一些外部依赖的情况下，可以选用redis。

这个是要系统设计者基于架构的考虑了

## 19.出现几率比较大的Redis面试题（含答案）

本文的面试题如下：

- Redis 持久化机制
- 缓存雪崩、缓存穿透、缓存预热、缓存更新、缓存降级等问题
- 热点数据和冷数据是什么
- Memcache与Redis的区别都有哪些？
- 单线程的redis为什么这么快
- redis的数据类型，以及每种数据类型的使用场景，Redis 内部结构
- redis的过期策略以及内存淘汰机制【～】
- Redis 为什么是单线程的，优点
- 如何解决redis的并发竞争key问题
- Redis 集群方案应该怎么做？都有哪些方案？
- 有没有尝试进行多机redis 的部署？如何保证数据一致的？
- 对于大量的请求怎么样处理
- Redis 常见性能问题和解决方案？
- 讲解下Redis线程模型
- 为什么Redis的操作是原子性的，怎么保证原子性的？
- Redis事务
- Redis实现分布式锁

------

### Redis 持久化机制

Redis是一个支持持久化的内存数据库，通过持久化机制把内存中的数据同步到硬盘文件来保证数据持久化。当Redis重启后通过把硬盘文件重新加载到内存，就能达到恢复数据的目的。

实现：单独创建fork()一个子进程，将当前父进程的数据库数据复制到子进程的内存中，然后由子进程写入到临时文件中，持久化的过程结束了，再用这个临时文件替换上次的快照文件，然后子进程退出，内存释放。

RDB是Redis默认的持久化方式。按照一定的时间周期策略把内存的数据以快照的形式保存到硬盘的二进制文件。即Snapshot快照存储，对应产生的数据文件为dump.rdb，通过配置文件中的save参数来定义快照的周期。（ 快照可以是其所表示的数据的一个副本，也可以是数据的一个复制品。）
AOF：Redis会将每一个收到的写命令都通过Write函数追加到文件最后，类似于MySQL的binlog。当Redis重启是会通过重新执行文件中保存的写命令来在内存中重建整个数据库的内容。

当两种方式同时开启时，数据恢复Redis会优先选择AOF恢复。

### 缓存雪崩、缓存穿透、缓存预热、缓存更新、缓存降级等问题

#### 一、**缓存雪崩**

缓存雪崩我们可以简单的理解为：由于原有缓存失效，新缓存未到期间

(例如：我们设置缓存时采用了相同的过期时间，在同一时刻出现大面积的缓存过期)，所有原本应该访问缓存的请求都去查询数据库了，而对数据库CPU和内存造成巨大压力，严重的会造成数据库宕机。从而形成一系列连锁反应，造成整个系统崩溃。

**解决办法**：

大多数系统设计者考虑用加锁（ 最多的解决方案）或者队列的方式保证来保证不会有大量的线程对数据库一次性进行读写，从而避免失效时大量的并发请求落到底层存储系统上。还有一个简单方案就时讲缓存失效时间分散开。

#### **二、缓存穿透**

缓存穿透是指用户查询数据，在数据库没有，自然在缓存中也不会有。这样就导致用户查询的时候，在缓存中找不到，每次都要去数据库再查询一遍，然后返回空（相当于进行了两次无用的查询）。这样请求就绕过缓存直接查数据库，这也是经常提的缓存命中率问题。

**解决办法：**

最常见的则是采用布隆过滤器，将所有可能存在的数据哈希到一个足够大的bitmap中，一个一定不存在的数据会被这个bitmap拦截掉，从而避免了对底层存储系统的查询压力。

另外也有一个更为简单粗暴的方法，如果一个查询返回的数据为空（不管是数据不存在，还是系统故障），我们仍然把这个空结果进行缓存，但它的过期时间会很短，最长不超过五分钟。通过这个直接设置的默认值存放到缓存，这样第二次到缓冲中获取就有值了，而不会继续访问数据库，这种办法最简单粗暴。

5TB的硬盘上放满了数据，请写一个算法将这些数据进行排重。如果这些数据是一些32bit大小的数据该如何解决？如果是64bit的呢？

对于空间的利用到达了一种极致，那就是Bitmap和布隆过滤器(Bloom Filter)。

**Bitmap：典型的就是哈希表**

缺点是，Bitmap对于每个元素只能记录1bit信息，如果还想完成额外的功能，恐怕只能靠牺牲更多的空间、时间来完成了。

**布隆过滤器（推荐）**

就是引入了k(k>1)k(k>1)个相互独立的哈希函数，保证在给定的空间、误判率下，完成元素判重的过程。

它的优点是空间效率和查询时间都远远超过一般的算法，缺点是有一定的误识别率和删除困难。

Bloom-Filter算法的核心思想就是利用多个不同的Hash函数来解决“冲突”。

Hash存在一个冲突（碰撞）的问题，用同一个Hash得到的两个URL的值有可能相同。为了减少冲突，我们可以多引入几个Hash，如果通过其中的一个Hash值我们得出某元素不在集合中，那么该元素肯定不在集合中。只有在所有的Hash函数告诉我们该元素在集合中时，才能确定该元素存在于集合中。这便是Bloom-Filter的基本思想。

Bloom-Filter一般用于在大数据量的集合中判定某元素是否存在。

**缓存穿透与缓存击穿的区别**

缓存击穿：是指一个key非常热点，在不停的扛着大并发，大并发集中对这一个点进行访问，当这个key在失效的瞬间，持续的大并发就穿破缓存，直接请求数据。

解决方案：在访问key之前，采用SETNX（set if not exists）来设置另一个短期key来锁住当前key的访问，访问结束再删除该短期key。

给一个我公司处理的案例：背景双机拿token，token在存一份到redis，保证系统在token过期时都只有一个线程去获取token;线上环境有两台机器，故使用分布式锁实现。

搜索公众号 Java面试题精选，回复“面试资料”，送你一份Java面试宝典.pdf

#### 三、缓存预热

缓存预热这个应该是一个比较常见的概念，相信很多小伙伴都应该可以很容易的理解，缓存预热就是系统上线后，将相关的缓存数据直接加载到缓存系统。这样就可以避免在用户请求的时候，先查询数据库，然后再将数据缓存的问题！用户直接查询事先被预热的缓存数据！

解决思路：

- 直接写个缓存刷新页面，上线时手工操作下；
- 数据量不大，可以在项目启动的时候自动进行加载；
- 定时刷新缓存；

#### 四、缓存更新

除了缓存服务器自带的缓存失效策略之外（Redis默认的有6中策略可供选择），我们还可以根据具体的业务需求进行自定义的缓存淘汰，常见的策略有两种：

- 定时去清理过期的缓存；
- 当有用户请求过来时，再判断这个请求所用到的缓存是否过期，过期的话就去底层系统得到新数据并更新缓存。

两者各有优劣，第一种的缺点是维护大量缓存的key是比较麻烦的，第二种的缺点就是每次用户请求过来都要判断缓存失效，逻辑相对比较复杂！具体用哪种方案，大家可以根据自己的应用场景来权衡。

#### 五、缓存降级

当访问量剧增、服务出现问题（如响应时间慢或不响应）或非核心服务影响到核心流程的性能时，仍然需要保证服务还是可用的，即使是有损服务。系统可以根据一些关键数据进行自动降级，也可以配置开关实现人工降级。

降级的最终目的是保证核心服务可用，即使是有损的。而且有些服务是无法降级的（如加入购物车、结算）。

以参考日志级别设置预案：

- 一般：比如有些服务偶尔因为网络抖动或者服务正在上线而超时，可以自动降级；
- 警告：有些服务在一段时间内成功率有波动（如在95~100%之间），可以自动降级或人工降级，并发送告警；
- 错误：比如可用率低于90%，或者数据库连接池被打爆了，或者访问量突然猛增到系统能承受的最大阀值，此时可以根据情况自动降级或者人工降级；
- 严重错误：比如因为特殊原因数据错误了，此时需要紧急人工降级。

服务降级的目的，是为了防止Redis服务故障，导致数据库跟着一起发生雪崩问题。因此，对于不重要的缓存数据，可以采取服务降级策略，例如一个比较常见的做法就是，Redis出现问题，不去数据库查询，而是直接返回默认值给用户。

搜索公众号 Java面试题精选，回复“面试资料”，送你一份Java面试宝典.pdf

### 热点数据和冷数据是什么

热点数据，缓存才有价值

对于冷数据而言，大部分数据可能还没有再次访问到就已经被挤出内存，不仅占用内存，而且价值不大。频繁修改的数据，看情况考虑使用缓存

对于上面两个例子，寿星列表、导航信息都存在一个特点，就是信息修改频率不高，读取通常非常高的场景。

对于热点数据，比如我们的某IM产品，生日祝福模块，当天的寿星列表，缓存以后可能读取数十万次。再举个例子，某导航产品，我们将导航信息，缓存以后可能读取数百万次。

**数据更新前至少读取两次，** 缓存才有意义。这个是最基本的策略，如果缓存还没有起作用就失效了，那就没有太大价值了。

那存不存在，修改频率很高，但是又不得不考虑缓存的场景呢？有！比如，这个读取接口对数据库的压力很大，但是又是热点数据，这个时候就需要考虑通过缓存手段，减少数据库的压力，比如我们的某助手产品的，点赞数，收藏数，分享数等是非常典型的热点数据，但是又不断变化，此时就需要将数据同步保存到Redis缓存，减少数据库压力。

### Memcache与Redis的区别都有哪些？

1)、存储方式 Memecache把数据全部存在内存之中，断电后会挂掉，数据不能超过内存大小。Redis有部份存在硬盘上，redis可以持久化其数据

2)、数据支持类型 memcached所有的值均是简单的字符串，redis作为其替代者，支持更为丰富的数据类型 ，提供list，set，zset，hash等数据结构的存储

3)、使用底层模型不同 它们之间底层实现方式 以及与客户端之间通信的应用协议不一样。Redis直接自己构建了VM 机制 ，因为一般的系统调用系统函数的话，会浪费一定的时间去移动和请求。

4). value 值大小不同：Redis 最大可以达到 512M；memcache 只有 1mb。

5）redis的速度比memcached快很多

6）Redis支持数据的备份，即master-slave模式的数据备份。

### 单线程的redis为什么这么快

(一)纯内存操作

(二)单线程操作，避免了频繁的上下文切换

(三)采用了非阻塞I/O多路复用机制

### redis的数据类型，以及每种数据类型的使用场景

回答：一共五种

#### (一)String

这个其实没啥好说的，最常规的set/get操作，value可以是String也可以是数字。一般做一些复杂的计数功能的缓存。

#### (二)hash

这里value存放的是结构化的对象，比较方便的就是操作其中的某个字段。博主在做单点登录的时候，就是用这种数据结构存储用户信息，以cookieId作为key，设置30分钟为缓存过期时间，能很好的模拟出类似session的效果。

#### (三)list

使用List的数据结构，可以做简单的消息队列的功能。另外还有一个就是，可以利用lrange命令，做基于redis的分页功能，性能极佳，用户体验好。本人还用一个场景，很合适—取行情信息。就也是个生产者和消费者的场景。LIST可以很好的完成排队，先进先出的原则。

#### (四)set

因为set堆放的是一堆不重复值的集合。所以可以做全局去重的功能。为什么不用JVM自带的Set进行去重？因为我们的系统一般都是集群部署，使用JVM自带的Set，比较麻烦，难道为了一个做一个全局去重，再起一个公共服务，太麻烦了。

另外，就是利用交集、并集、差集等操作，可以计算共同喜好，全部的喜好，自己独有的喜好等功能。

#### (五)sorted set

sorted set多了一个权重参数score,集合中的元素能够按score进行排列。可以做排行榜应用，取TOP N操作。

### Redis 内部结构

- dict 本质上是为了解决算法中的查找问题（Searching）是一个用于维护key和value映射关系的数据结构，与很多语言中的Map或dictionary类似。本质上是为了解决算法中的查找问题（Searching）
- sds sds就等同于char * 它可以存储任意二进制数据，不能像C语言字符串那样以字符’\0’来标识字符串的结 束，因此它必然有个长度字段。
- skiplist （跳跃表） 跳表是一种实现起来很简单，单层多指针的链表，它查找效率很高，堪比优化过的二叉平衡树，且比平衡树的实现，
- quicklist
- ziplist 压缩表 ziplist是一个编码后的列表，是由一系列特殊编码的连续内存块组成的顺序型数据结构，

### redis的过期策略以及内存淘汰机制

redis采用的是定期删除+惰性删除策略。

**为什么不用定时删除策略?**

定时删除,用一个定时器来负责监视key,过期则自动删除。虽然内存及时释放，但是十分消耗CPU资源。在大并发请求下，CPU要将时间应用在处理请求，而不是删除key,因此没有采用这一策略.

**定期删除+惰性删除是如何工作的呢?**

定期删除，redis默认每个100ms检查，是否有过期的key,有过期key则删除。需要说明的是，redis不是每个100ms将所有的key检查一次，而是随机抽取进行检查(如果每隔100ms,全部key进行检查，redis岂不是卡死)。因此，如果只采用定期删除策略，会导致很多key到时间没有删除。

于是，惰性删除派上用场。也就是说在你获取某个key的时候，redis会检查一下，这个key如果设置了过期时间那么是否过期了？如果过期了此时就会删除。

**采用定期删除+惰性删除就没其他问题了么?**

不是的，如果定期删除没删除key。然后你也没即时去请求key，也就是说惰性删除也没生效。这样，redis的内存会越来越高。那么就应该采用内存淘汰机制。

在redis.conf中有一行配置

```
maxmemory-policy volatile-lru1
```

该配置就是配内存淘汰策略的(什么，你没配过？好好反省一下自己)

- volatile-lru：从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰
- volatile-ttl：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰
- volatile-random：从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰
- allkeys-lru：从数据集（server.db[i].dict）中挑选最近最少使用的数据淘汰
- allkeys-random：从数据集（server.db[i].dict）中任意选择数据淘汰
- no-enviction（驱逐）：禁止驱逐数据，新写入操作会报错

ps：如果没有设置 expire 的key, 不满足先决条件(prerequisites); 那么 volatile-lru, volatile-random 和 volatile-ttl 策略的行为, 和 noeviction(不删除) 基本上一致。

### Redis 为什么是单线程的

官方FAQ表示，因为Redis是基于内存的操作，CPU不是Redis的瓶颈，Redis的瓶颈最有可能是机器内存的大小或者网络带宽。

既然单线程容易实现，而且CPU不会成为瓶颈，那就顺理成章地采用单线程的方案了（毕竟采用多线程会有很多麻烦！）Redis利用队列技术将并发访问变为串行访问

1）绝大部分请求是纯粹的内存操作（非常快速）

2）采用单线程,避免了不必要的上下文切换和竞争条件

3）非阻塞IO优点：

- 速度快，因为数据存在内存中，类似于HashMap，HashMap的优势就是查找和操作的时间复杂度都是O(1)
- 支持丰富数据类型，支持string，list，set，sorted set，hash
- 支持事务，操作都是原子性，所谓的原子性就是对数据的更改要么全部执行，要么全部不执行
- 丰富的特性：可用于缓存，消息，按key设置过期时间，过期后将会自动删除如何解决redis的并发竞争key问题

**同时有多个子系统去set一个key。这个时候要注意什么呢？**

不推荐使用redis的事务机制。因为我们的生产环境，基本都是redis集群环境，做了数据分片操作。你一个事务中有涉及到多个key操作的时候，这多个key不一定都存储在同一个redis-server上。因此，redis的事务机制，十分鸡肋。

- 如果对这个key操作，不要求顺序：准备一个分布式锁，大家去抢锁，抢到锁就做set操作即可
- 如果对这个key操作，要求顺序：分布式锁+时间戳。假设这会系统B先抢到锁，将key1设置为{valueB 3:05}。接下来系统A抢到锁，发现自己的valueA的时间戳早于缓存中的时间戳，那就不做set操作了。以此类推。
- 利用队列，将set方法变成串行访问也可以redis遇到高并发，如果保证读写key的一致性

对redis的操作都是具有原子性的,是线程安全的操作,你不用考虑并发问题,redis内部已经帮你处理好并发的问题了。

### Redis 集群方案应该怎么做？都有哪些方案？

1.twemproxy，大概概念是，它类似于一个代理方式， 使用时在本需要连接 redis 的地方改为连接 twemproxy， 它会以一个代理的身份接收请求并使用一致性 hash 算法，将请求转接到具体 redis，将结果再返回 twemproxy。

缺点：twemproxy 自身单端口实例的压力，使用一致性 hash 后，对 redis 节点数量改变时候的计算值的改变，数据无法自动移动到新的节点。

2.codis，目前用的最多的集群方案，基本和 twemproxy 一致的效果，但它支持在 节点数量改变情况下，旧节点数据可恢复到新 hash 节点

3.redis cluster3.0 自带的集群，特点在于他的分布式算法不是一致性 hash，而是 hash 槽的概念，以及自身支持节点设置从节点。具体看官方文档介绍。

### 有没有尝试进行多机redis 的部署？如何保证数据一致的？

主从复制，读写分离

一类是主数据库（master）一类是从数据库（slave），主数据库可以进行读写操作，当发生写操作的时候自动将数据同步到从数据库，而从数据库一般是只读的，并接收主数据库同步过来的数据，一个主数据库可以有多个从数据库，而一个从数据库只能有一个主数据库。

### 对于大量的请求怎么样处理

redis是一个单线程程序，也就说同一时刻它只能处理一个客户端请求；

redis是通过IO多路复用（select，epoll, kqueue，依据不同的平台，采取不同的实现）来处理多个客户端请求的

### Redis 常见性能问题和解决方案？

(1) Master 最好不要做任何持久化工作，如 RDB 内存快照和 AOF 日志文件

(2) 如果数据比较重要，某个 Slave 开启 AOF 备份数据，策略设置为每秒同步一次

(3) 为了主从复制的速度和连接的稳定性， Master 和 Slave 最好在同一个局域网内

(4) 尽量避免在压力很大的主库上增加从库

(5) 主从复制不要用图状结构，用单向链表结构更为稳定，即：Master <- Slave1 <- Slave2 <-
Slave3…

往期面试题汇总：[001期~150期汇总](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247485351&idx=2&sn=214225ab4345f4d9c562900cb42a52ba&chksm=e80db1d1df7a38c741137246bf020a5f8970f74cd03530ccc4cb2258c1ced68e66e600e9e059&scene=21#wechat_redirect)

### 讲解下Redis线程模型

文件事件处理器包括分别是套接字、 I/O 多路复用程序、 文件事件分派器（dispatcher）、 以及事件处理器。使用 I/O 多路复用程序来同时监听多个套接字， 并根据套接字目前执行的任务来为套接字关联不同的事件处理器。

当被监听的套接字准备好执行连接应答（accept）、读取（read）、写入（write）、关闭（close）等操作时， 与操作相对应的文件事件就会产生， 这时文件事件处理器就会调用套接字之前关联好的事件处理器来处理这些事件。

I/O 多路复用程序负责监听多个套接字， 并向文件事件分派器传送那些产生了事件的套接字。

**工作原理：**

I/O 多路复用程序负责监听多个套接字， 并向文件事件分派器传送那些产生了事件的套接字。

尽管多个文件事件可能会并发地出现， 但 I/O 多路复用程序总是会将所有产生事件的套接字都入队到一个队列里面， 然后通过这个队列， 以有序（sequentially）、同步（synchronously）、每次一个套接字的方式向文件事件分派器传送套接字：

当上一个套接字产生的事件被处理完毕之后（该套接字为事件所关联的事件处理器执行完毕）， I/O 多路复用程序才会继续向文件事件分派器传送下一个套接字。如果一个套接字又可读又可写的话， 那么服务器将先读套接字， 后写套接字.
![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBxyw4zwzHrN87OtSSlrHMpkWRhu8yrbVJvvM0YqnElXY5hLtLFPEtvhyAGEMYr39LQ7tg1xLBrow/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 为什么Redis的操作是原子性的，怎么保证原子性的？

对于Redis而言，命令的原子性指的是：一个操作的不可以再分，操作要么执行，要么不执行。

Redis的操作之所以是原子性的，是因为Redis是单线程的。（Redis新版本已经引入多线程，这里基于旧版本的Redis）

Redis本身提供的所有API都是原子操作，Redis中的事务其实是要保证批量操作的原子性。

多个命令在并发中也是原子性的吗？

不一定， 将get和set改成单命令操作，incr 。使用Redis的事务，或者使用Redis+Lua==的方式实现.

### Redis事务

Redis事务功能是通过MULTI、EXEC、DISCARD和WATCH 四个原语实现的

Redis会将一个事务中的所有命令序列化，然后按顺序执行。

1. redis 不支持回滚“Redis 在事务失败时不进行回滚，而是继续执行余下的命令”， 所以 Redis 的内部可以保持简单且快速。
2. 如果在一个事务中的命令出现错误，那么所有的命令都不会执行；
3. 如果在一个事务中出现运行错误，那么正确的命令会被执行。

注：redis的discard只是结束本次事务,正确命令造成的影响仍然存在.

1）MULTI命令用于开启一个事务，它总是返回OK。MULTI执行之后，客户端可以继续向服务器发送任意多条命令，这些命令不会立即被执行，而是被放到一个队列中，当EXEC命令被调用时，所有队列中的命令才会被执行。

2）EXEC：执行所有事务块内的命令。返回事务块内所有命令的返回值，按命令执行的先后顺序排列。当操作被打断时，返回空值 nil 。

3）通过调用DISCARD，客户端可以清空事务队列，并放弃执行事务， 并且客户端会从事务状态中退出。

4）WATCH 命令可以为 Redis 事务提供 check-and-set （CAS）行为。可以监控一个或多个键，一旦其中有一个键被修改（或删除），之后的事务就不会执行，监控一直持续到EXEC命令。

### Redis实现分布式锁

Redis为单进程单线程模式，采用队列模式将并发访问变成串行访问，且多客户端对Redis的连接并不存在竞争关系Redis中可以使用SETNX命令实现分布式锁。

将 key 的值设为 value ，当且仅当 key 不存在。若给定的 key 已经存在，则 SETNX 不做任何动作
![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XBxyw4zwzHrN87OtSSlrHMpVS1DFtnc4SOvhepF8szTNNiaibwd5KALPMhmb69Z0fKkY23q77XB8BkQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

解锁：使用 del key 命令就能释放锁

解决死锁：

- 通过Redis中expire()给锁设定最大持有时间，如果超过，则Redis来帮我们释放锁。
- 使用 setnx key “当前系统时间+锁持有的时间”和getset key “当前系统时间+锁持有的时间”组合的命令就可以实现。

## 20.Redis——第三方jar没有封装的命令我们该怎么执行

### 一、Pipelin模式介绍

#### 1、redis的通常使用方式

大多数情况下，我们都会通过请求-相应机制去操作redis。使用这种模式的步骤为

1. 获得jedis实例
2. 发送redis命令
3. 由于redis是单线程的，所以处理完上一个指令之后才会进行执行该命令。

整个交互流程如下

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7UpwkqvxmPQQxD6BcZL7PjHQTRetHQztL4tEkans5ebxSw9ayKvIBoQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 2、Pipeline模式

然而使用Pipeline 模式，客户端可以一次性的发送多个命令，无需等待服务端返回。这样就大大的减少了网络往返时间，提高了系统性能。

pipeline是多条命令的组合，使用PIPELINE 可以解决网络开销的问题，原理也非常简单,流程如下, 将多个指令打包后,一次性提交到Redis, 网络通信只有一次

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7by0It0yugnBuQAnWjxsEsREogMyUkbZUeFQgZSfPh7uLbYnq7bjicIQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 3、性能对比

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7MCHiaVKyF1Kibib4GibvKlvVI6wLEd2jdZCDawNAq0ffT68Q8Fm2k10XwQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到，redis的延迟主要出现在网络请求的IO次数上，因此我们在使用redis的时候，尽量减少网络IO次数，通过pipeline的方式将多个指令封装在一个命令里执行。

### 二、Redis事物

redis的简单事务是将一组需要一起执行的命令放到multi和exec两个命令之间，其中multi代表事务开始，exec代表事务结束

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7EvObr9RwJEXwD9ByRVMicib7ywiaQmyaiameZYs2FibxvUpL5FBgeoV4XmQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 1、事务命令

- multi:事务开始
- exec:提交事务
- watch:事务监控

WATCH命令可以监控一个或多个键，一旦其中有一个键被修改（或删除），之后的事务就不会执行。监控一直持续到

discard:停止事务

在执行exec之前执行该命令，提交事务会失败，执行的命令会进行回滚

```
127.0.0.1:6379> multi     //开始事务
 
OK
 
127.0.0.1:6379> sadd tt 1   //业务操作
 
QUEUED
 
127.0.0.1:6379> DISCARD   //停止事务
 
OK
 
127.0.0.1:6379> exec   //提交事务
 
(error) ERR EXEC without MULTI   //报不存在事务异常
 
127.0.0.1:6379> get tt  //获取不到对象
 
(nil)
 
127.0.0.1:6379>
```

#### 2、事务异常

redis支持事务，但他属于弱事务，中间的一些异常可能会导致事务失效。

往期面试题：[001期~180期汇总](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247486073&idx=2&sn=a26a44e561a468d94be99761ab5fc1fc&chksm=e80dbc0fdf7a3519999f1fdf0ee5b98b12a519c539f3ede9152ef66e4a3fb6a7f771cc2db382&scene=21#wechat_redirect)

1、命令错误，语法不正确，导致事务不能正常结束

```
127.0.0.1:6379> multi   //开始事务
 
OK
 
127.0.0.1:6379> set aa 123  //业务操作
 
QUEUED
 
127.0.0.1:6379> sett bb 124  //命令错误
 
(error) ERR unknown command 'sett'
 
127.0.0.1:6379> exec 
 
(error) EXECABORT Transaction discarded because of  previous errors.  //提交事务异常
 
127.0.0.1:6379> get aa  //查询不到数据
 
(nil)
 
127.0.0.1:6379>
```

2、运行错误，语法正确，但类型错误，事务可以正常结束

```
127.0.0.1:6379> multi
 
OK
 
127.0.0.1:6379> set t 1   //业务操作1
 
QUEUED
 
127.0.0.1:6379> sadd t 1  //业务操作2
 
QUEUED
 
127.0.0.1:6379> set t 2  //业务操作3
 
QUEUED
 
127.0.0.1:6379> exec
 
1) OK
 
2) (error) WRONGTYPE Operation against a key holding the wrong kind of value  //类型异常
 
3) OK
 
127.0.0.1:6379> get t  //可以获取到t
 
"2"
 
127.0.0.1:6379>
```

### 三、redis发布与订阅

redis提供了“发布、订阅”模式的消息机制，其中消息订阅者与发布者不直接通信，发布者向指定的频道（channel）发布消息，订阅该频道的每个客户端都可以接收到消息

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7LWusYBFRA7gbK9cXq6sgKA3ZwOJwicP3kAbcS6iaspMDYbficxeuW15LA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 1、Redis发布订阅常用命令

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7Y8VHCphXg8k3P4KjtNuo9g5vOH5HOIbslR0dmCfGFFhFibicG8Ziaoo7g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 2、性能测试

参考:

> https://blog.csdn.net/b379685397/article/details/95626295

#### 3、应用场景

redis主要提供发布消息、订阅频道、取消订阅以及按照模式订阅和取消订阅，和很多专业的消息队列（kafka rabbitmq）,redis的发布订阅显得很lower, 比如无法实现消息规程和回溯， 但就是简单，如果能满足应用场景，用这个也可以

1. 订阅号、公众号、微博关注、邮件订阅系统等
2. 即使通信系统
3. 群聊部落系统（微信群）

### 四、键的迁移

键迁移大家可能用的不是很多，因为一般都是使用redis主从同步。不过对于我们做数据统计分析使用的时候，可能会使用到，比如用户标签。为了避免key批量删除导致的redis雪崩，一般都是通过一个计算使用的redis和一个最终业务使用的redis，通过将计算时用的redis里的键值通过迁移的方式一个一个的更新到业务redis中，使其对业务冲击最小化。

#### 1、move

move指令将redis一个库中的数据迁移到另外一个库中。

```
move key db  //reids有16个库， 编号为0－15
set name DK;  move name 5 //迁移到第6个库
elect 5 ;//数据库切换到第6个库，
get name  可以取到james1
```

如果key在目标数据库中已存在，那么什么也不会发生。这种模式不建议在生产环境使用，在同一个reids里可以玩

#### 2、dump

Redis DUMP 命令用于将key给序列化 ，并返回被序列化的值。用于导入到其他服务中

一般通过dump命令导出，使用restore命令导入。

1,在A服务器上

```
set name james;
dump name;       //  得到"\x00\x05james\b\x001\x82;f\"DhJ"
```

2,在B服务器上

```
restore name 0 "\x00\x05james\b\x001\x82;f\"DhJ"    //0代表没有过期时间
get name           //返回james
```

#### 3、migrate

migrate用于在Redis实例间进行数据迁移，实际上migrate命令是将dump、restore、del三个命令进行组合，从而简化了操作流程。

往期面试题：[001期~180期汇总](http://mp.weixin.qq.com/s?__biz=MzIyNDU2ODA4OQ==&mid=2247486073&idx=2&sn=a26a44e561a468d94be99761ab5fc1fc&chksm=e80dbc0fdf7a3519999f1fdf0ee5b98b12a519c539f3ede9152ef66e4a3fb6a7f771cc2db382&scene=21#wechat_redirect)

migrate命令具有原子性，从Redis 3.0.6版本后已经支持迁移多个键的功能。migrate命令的数据传输直接在源Redis和目标Redis上完成，目标Redis完成restore后会发送OK给源Redis。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7Rn0lvCib4eiag9iabLIJkfhn0b4uENh8MbuOibicHpHSLf1991N0HiaKuicbw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

比如：把111上的name键值迁移到112上的redis

```
192.168.42.111:6379> migrate 192.168.42.112 6379 name 0 1000 copy
```

### 五、自定义命令封装

当我们使用jedis或者jdbctemplate时，想执行键迁移的指令的时候，发现根本没有给我们封装相关指令，这个时候我们该怎么办呢？除了框架帮我们封装的方法外，我们自己也可以通过反射的方式进行命令的封装，主要步骤如下

1. 建立Connection链接，使用Connection连接Redis
2. 通过反射获取Connection中的sendCommand方法（protected Connection sendCommand(Command cmd, String... args)）。
3. 调用connection的sendCommand方法，第二个参数为执行的命令（比如set,get,client等），第三个参数为命令的参数。可以看到ProtocolCommand这个枚举对象包含了redis的所有指令，即所有的指令都可以通过这个对象获取到。并封装执行
4. 执行invoke方法，并且按照redis的指令封装参数
5. 获取Redis的命令执行结果

```
package com.james.cache.jedis;
 
import redis.clients.jedis.Connection;
import redis.clients.jedis.Protocol;
 
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 
/**
 * @Auther: DK
 * @Date: 2020/10/11 23:17
 * @Description:
 */
public class RedisKeyMove {
 
    public static void main(String[] args) throws IOException {
        //1.使用Connection连接Redis
        try (Connection connection = new Connection("10.1.253.188", 6379)) {
            // 2. 通过反射获取Connection中的sendCommand方法（protected Connection sendCommand(Command cmd, String... args)）。
            Method method = Connection.class.getDeclaredMethod("sendCommand", Protocol.Command.class, String[].class);
            method.setAccessible(true); // 设置可以访问private和protected方法
            // 3. 调用connection的sendCommand方法，第二个参数为执行的命令（比如set,get,client等），第三个参数为命令的参数。
            // 3.1 该命令最终对应redis中为: set test-key test-value
            method.invoke(connection, Protocol.Command.MIGRATE,
                    new String[] {"10.1.253.69", "6379", "name", "0", "1000", "copy"});
            // 4.获取Redis的命令执行结果
            System.out.println(connection.getBulkReply());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
```

### 六、键全量遍历

#### 1、keys

![图片](https://mmbiz.qpic.cn/mmbiz_png/8KKrHK5ic6XD7btutQeW496oSsxGWViaG7s7PDOOKdpfe7d8Db3VzV0C3CicfcSbiaOY16ozuaaJ4tLVD2rIticIneQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

考虑到是单线程，使用改命令会阻塞线程， 在生产环境不建议使用，键多可能会阻塞。

#### 2、渐进式遍历 scan

1，初始化数据

```
mset n1 1 n2 2 n3 3 n4 4 n5 5 n6 6 n7 7 n8 8 n9 9 n10 10 n11 11 n12 12 n13 13
```

2，遍历匹配

```
scan 0 match n* count 5    匹配以n开头的键，最大是取5条，第一次scan 0开始
```

第二次从游标4096开始取20个以n开头的键，相当于一页一页的取当最后返回0时，键被取完。

#### 3、scan 和keys对比

1. 通过游标分布进行的，不会阻塞线程;
2. 提供 limit 参数，可以控制每次返回结果的最大条数，limit 不准，返回的结果可多可少;
3. 同 keys 一样，Scan也提供模式匹配功能;
4. 服务器不需要为游标保存状态，游标的唯一状态就是 scan 返回给客户端的游标整数;
5. scan返回的结果可能会有重复，需要客户端去重复;
6. scan遍历的过程中如果有数据修改，改动后的数据能不能遍历到是不确定的;
7. 单次返回的结果是空的并不意味着遍历结束，而要看返回的游标值是否为零;

#### 4、其他遍历命令

SCAN 命令用于迭代当前数据库中的数据库键。

SSCAN 命令用于迭代集合键中的元素。

HSCAN 命令用于迭代哈希键中的键值对。

ZSCAN 命令用于迭代有序集合中的元素（包括元素成员和元素分值）。

用法和scan一样

## 21.说说常用的Redis和Zookeeper两种分布式锁的对比

## 22.你能说一下Redis的常见应用场景吗

### 1. 基础

#### 内存数据库

Redis是一个key-value型的数据库（相比较之下，MySQL是关联数据库），也就是说，一个key对应一个value，这是保证高效的手段之一。另外，Redis的所有数据在使用时都存放在内存中。

这包含了两层含义：

1. 单台Redis能存放多少数据，取决于其内存的大小（假设所有内存都给Redis用）。如果需要存放更多数据，可以增加内存或做集群。
2. Redis支持将数据持久化到磁盘中。

但是，不会直接对磁盘进行读写。这种持久化，一般是用于在服务器重启时，先把数据持久化，重启后再从磁盘中读取到内存。

#### 数据结构

Redis支持五种数据结构，分别是String，List，Hash，Set，Zset。即字符串，列表，哈希，集合，有序集合。

**String是Redis最基本的类型，一个key对应一个value。**

一般情况下，大部分的内容都可以通过序列化后，再存在到Redis中，比如图片或对象等。每个key对就的value存储的内容最大为512M。

**Hash即哈希表，即key-value对集合。**

是不是很奇怪？Redis的数据本身不就是key-value型的吗？其实不奇怪。我们这里在说数据结构的时候，单指的是key-value中的value。也就是说，value是一个key-value对集合。想象一下这种数据结构，特别适合存储对象。并且，Redis支持像数据库中update一样，单独修改对象的某个属性。

**List即列表。**

value是一个字符串的列表。也就是说，一个value可以存放多个字符串，可以按照顺序，添加到头或尾。它就是一个双向链表。很适合做如朋友圈动态列表或消息队列等。

**Set即集合。**

它的value和列表的value一样，也是一个字符串列表，只是Set是无序的，并且，value中的元素是不重复的。和Java中的Set差不多，它的基础原理也是基于Hash实现的，所以添加、删除、查找等的效率等都很快。Redis还为Set提供了多个集合操作的API，如交集、并集、差集等。可以利用来做统计，有多少个共同好友等。

**Zset即有序集合。**

它在Set的基础上，给value中的每个字符串关联了一个score属性，即得分。Zset通过计算得分，将字符串进行从小到大的排序。字符串的得分可以相同。Zset的排序是在插入时直接就做好的。可以用来做排行榜等。

### 2. Redis常出现的应用场景

#### 缓存——热数据

热点数据（经常会被查询，但是不经常被修改或者删除的数据），首选是使用redis缓存，毕竟强大到冒泡的QPS和极强的稳定性不是所有类似工具都有的，而且相比于memcached还提供了丰富的数据类型可以使用，另外，内存中的数据也提供了AOF和RDB等持久化机制可以选择，要冷、热的还是忽冷忽热的都可选。

结合具体应用需要注意一下：很多人用spring的AOP来构建redis缓存的自动生产和清除，过程一般如下：step1-> Select 数据库前查询redis，有的话使用redis数据，放弃select 数据库，没有就select 数据库，然后将数据插入redis; srep2-> update或者delete数据库钱，查询redis是否存在该数据，存在的话先删除redis中数据，然后再update或者delete数据库中的数据。 这种操作，如果并发量很小的情况下基本没问题，但是高并发的情况请注意下面场景：

为了update先删掉了redis中的该数据，这时候另一个线程执行查询，发现redis中没有，瞬间执行了查询SQL，并且插入到redis中一条数据，回到刚才那个update语句，这个悲催的线程压根不知道刚才那个该死的select线程犯了一个弥天大错！于是这个redis中的错误数据就永远的存在了下去，直到下一个update或者delete。

#### 计数器

诸如统计点击数等应用。由于单线程，可以避免并发问题，保证不会出错，而且100%毫秒级性能。

#### 队列

相当于消息系统，与ActiveMQ，RocketMQ等工具类似，但是觉得简单用一下还行，如果对于数据一致性要求高的话还是用RocketMQ等专业系统。

由于redis把数据添加到队列是返回添加元素在队列的第几位，所以可以做判断用户是第几个访问这种业务。队列不仅可以把并发请求变成串行，并且还可以做队列或者栈使用。

#### 位操作（大数据处理）

用于数据量上亿的场景下，例如几亿用户系统的签到，去重登录次数统计，某用户是否在线状态等等。腾讯10亿用户，要几个毫秒内查询到某个用户是否在线，能怎么做？

千万别说给每个用户建立一个key，然后挨个记（你可以算一下需要的内存会很恐怖，而且这种类似的需求很多。这里要用到位操作——使用setbit、getbit、bitcount命令。原理是：

redis内构建一个足够长的数组，每个数组元素只能是0和1两个值，然后这个数组的下标index用来表示用户id（必须是数字哈），那么很显然，这个几亿长的大数组就能通过下标和元素值（0和1）来构建一个记忆系统。

#### 最新列表

例如新闻列表页面的最新的新闻列表，如果总数量很大的情况下，尽量不要使用select a from A limit 10这种low货，尝试redis的 LPUSH命令构建List，一个个顺序都塞进去就可以啦。不过万一内存清掉了咋办？也简单，查询不到存储key的话，用mysql查询并且初始化一个List到redis中就好了。

#### 排行榜

### 3. 参考资料

- https://baijiahao.baidu.com/s?id=1636565352949240200
- https://www.cnblogs.com/NiceCui/p/7794659.html

## 23.Redis常见的面试题

### 说说Redis基本数据类型有哪些吧

1. 字符串：redis没有直接使用C语言传统的字符串表示，而是自己实现的叫做简单动态字符串SDS的抽象类型。C语言的字符串不记录自身的长度信息，而SDS则保存了长度信息，这样将获取字符串长度的时间由O(N)降低到了O(1)，同时可以避免缓冲区溢出和减少修改字符串长度时所需的内存重分配次数。
2. 链表linkedlist：redis链表是一个双向无环链表结构，很多发布订阅、慢查询、监视器功能都是使用到了链表来实现，每个链表的节点由一个listNode结构来表示，每个节点都有指向前置节点和后置节点的指针，同时表头节点的前置和后置节点都指向NULL。
3. 字典hashtable：用于保存键值对的抽象数据结构。redis使用hash表作为底层实现，每个字典带有两个hash表，供平时使用和rehash时使用，hash表使用链地址法来解决键冲突，被分配到同一个索引位置的多个键值对会形成一个单向链表，在对hash表进行扩容或者缩容的时候，为了服务的可用性，rehash的过程不是一次性完成的，而是渐进式的。
4. 跳跃表skiplist：跳跃表是有序集合的底层实现之一，redis中在实现有序集合键和集群节点的内部结构中都是用到了跳跃表。redis跳跃表由zskiplist和zskiplistNode组成，zskiplist用于保存跳跃表信息（表头、表尾节点、长度等），zskiplistNode用于表示表跳跃节点，每个跳跃表的层高都是1-32的随机数，在同一个跳跃表中，多个节点可以包含相同的分值，但是每个节点的成员对象必须是唯一的，节点按照分值大小排序，如果分值相同，则按照成员对象的大小排序。
5. 整数集合intset：用于保存整数值的集合抽象数据结构，不会出现重复元素，底层实现为数组。
6. 压缩列表ziplist：压缩列表是为节约内存而开发的顺序性数据结构，他可以包含多个节点，每个节点可以保存一个字节数组或者整数值。

基于这些基础的数据结构，redis封装了自己的对象系统，包含字符串对象string、列表对象list、哈希对象hash、集合对象set、有序集合对象zset，每种对象都用到了至少一种基础的数据结构。

redis通过encoding属性设置对象的编码形式来提升灵活性和效率，基于不同的场景redis会自动做出优化。不同对象的编码如下：

1. 字符串对象string：int整数、embstr编码的简单动态字符串、raw简单动态字符串
2. 列表对象list：ziplist、linkedlist
3. 哈希对象hash：ziplist、hashtable
4. 集合对象set：intset、hashtable
5. 有序集合对象zset：ziplist、skiplist

### Redis为什么快呢？

redis的速度非常的快，单机的redis就可以支撑每秒10几万的并发，相对于mysql来说，性能是mysql的几十倍。速度快的原因主要有几点：

1. 完全基于内存操作
2. C语言实现，优化过的数据结构，基于几种基础的数据结构，redis做了大量的优化，性能极高
3. 使用单线程，无上下文的切换成本
4. 基于非阻塞的IO多路复用机制

### 那为什么Redis6.0之后又改用多线程呢?

redis使用多线程并非是完全摒弃单线程，redis还是使用单线程模型来处理客户端的请求，只是使用多线程来处理数据的读写和协议解析，执行命令还是使用单线程。

这样做的目的是因为redis的性能瓶颈在于网络IO而非CPU，使用多线程能提升IO读写的效率，从而整体提高redis的性能。

### 知道什么是热key吗？热key问题怎么解决？

所谓热key问题就是，突然有几十万的请求去访问redis上的某个特定key，那么这样会造成流量过于集中，达到物理网卡上限，从而导致这台redis的服务器宕机引发雪崩。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAot2hSYkmxjb2lppicYuBMrmRia0jLg8zCMveicIHwbnFZ3dm7xo0xzyNOuA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

针对热key的解决方案：

1. 提前把热key打散到不同的服务器，降低压力
2. 加入二级缓存，提前加载热key数据到内存中，如果redis宕机，走内存查询

### 什么是缓存击穿、缓存穿透、缓存雪崩？

#### 缓存击穿

缓存击穿的概念就是单个key并发访问过高，过期时导致所有请求直接打到db上，这个和热key的问题比较类似，只是说的点在于过期导致请求全部打到DB上而已。

解决方案：

1. 加锁更新，比如请求查询A，发现缓存中没有，对A这个key加锁，同时去数据库查询数据，写入缓存，再返回给用户，这样后面的请求就可以从缓存中拿到数据了。
2. 将过期时间组合写在value中，通过异步的方式不断的刷新过期时间，防止此类现象。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAot15ia9NJVxN1r9QDltntLmqC5MxBBlIOOBDvnvNBiciaYXFcOut4ZD5p1g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)https://tva

#### 缓存穿透

缓存穿透是指查询不存在缓存中的数据，每次请求都会打到DB，就像缓存不存在一样。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAotfyfg1VfmxRiaxwqyukuox5QNiazUlicn7FJ9Anicbl8bPGf25VMYNYhKuQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

针对这个问题，加一层布隆过滤器。布隆过滤器的原理是在你存入数据的时候，会通过散列函数将它映射为一个位数组中的K个点，同时把他们置为1。

这样当用户再次来查询A，而A在布隆过滤器值为0，直接返回，就不会产生击穿请求打到DB了。

显然，使用布隆过滤器之后会有一个问题就是误判，因为它本身是一个数组，可能会有多个值落到同一个位置，那么理论上来说只要我们的数组长度够长，误判的概率就会越低，这种问题就根据实际情况来就好了。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAotwmfeKnHQeeQqYdkcqnYV61WpN3SKnJCxqpAZ1XauEAz6WyooC542KA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 缓存雪崩

当某一时刻发生大规模的缓存失效的情况，比如你的缓存服务宕机了，会有大量的请求进来直接打到DB上，这样可能导致整个系统的崩溃，称为雪崩。雪崩和击穿、热key的问题不太一样的是，他是指大规模的缓存都过期失效了。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAot0TVVVSI3kCN5mQIUwIU0jZeaZylKHeFRibbf473TNs9fF1Ut0Gg3rAg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

针对雪崩几个解决方案：

1. 针对不同key设置不同的过期时间，避免同时过期
2. 限流，如果redis宕机，可以限流，避免同时刻大量请求打崩DB
3. 二级缓存，同热key的方案。

### Redis的过期策略有哪些？

redis主要有2种过期删除策略

#### 惰性删除

惰性删除指的是当我们查询key的时候才对key进行检测，如果已经达到过期时间，则删除。显然，他有一个缺点就是如果这些过期的key没有被访问，那么他就一直无法被删除，而且一直占用内存。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAotTUJJNic94cee1OKFlibBEJpiazwQu2YRicbyicicpYiboFYicnDETibNddYdO7A/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 定期删除

定期删除指的是redis每隔一段时间对数据库做一次检查，删除里面的过期key。由于不可能对所有key去做轮询来删除，所以redis会每次随机取一些key去做检查和删除。

#### 那么定期+惰性都没有删除过期的key怎么办？

假设redis每次定期随机查询key的时候没有删掉，这些key也没有做查询的话，就会导致这些key一直保存在redis里面无法被删除，这时候就会走到redis的内存淘汰机制。

1. volatile-lru：从已设置过期时间的key中，移出最近最少使用的key进行淘汰
2. volatile-ttl：从已设置过期时间的key中，移出将要过期的key
3. volatile-random：从已设置过期时间的key中随机选择key淘汰
4. allkeys-lru：从key中选择最近最少使用的进行淘汰
5. allkeys-random：从key中随机选择key进行淘汰
6. noeviction：当内存达到阈值的时候，新写入操作报错

### 持久化方式有哪些？有什么区别？

redis持久化方案分为RDB和AOF两种。

#### RDB

RDB持久化可以手动执行也可以根据配置定期执行，它的作用是将某个时间点上的数据库状态保存到RDB文件中，RDB文件是一个压缩的二进制文件，通过它可以还原某个时刻数据库的状态。由于RDB文件是保存在硬盘上的，所以即使redis崩溃或者退出，只要RDB文件存在，就可以用它来恢复还原数据库的状态。

可以通过SAVE或者BGSAVE来生成RDB文件。

SAVE命令会阻塞redis进程，直到RDB文件生成完毕，在进程阻塞期间，redis不能处理任何命令请求，这显然是不合适的。

BGSAVE则是会fork出一个子进程，然后由子进程去负责生成RDB文件，父进程还可以继续处理命令请求，不会阻塞进程。

#### AOF

AOF和RDB不同，AOF是通过保存redis服务器所执行的写命令来记录数据库状态的。

AOF通过追加、写入、同步三个步骤来实现持久化机制。

1. 当AOF持久化处于激活状态，服务器执行完写命令之后，写命令将会被追加append到aof_buf缓冲区的末尾
2. 在服务器每结束一个事件循环之前，将会调用flushAppendOnlyFile函数决定是否要将aof_buf的内容保存到AOF文件中，可以通过配置appendfsync来决定。

```
always ##aof_buf内容写入并同步到AOF文件
everysec ##将aof_buf中内容写入到AOF文件，如果上次同步AOF文件时间距离现在超过1秒，则再次对AOF文件进行同步
no ##将aof_buf内容写入AOF文件，但是并不对AOF文件进行同步，同步时间由操作系统决定
```

如果不设置，默认选项将会是everysec，因为always来说虽然最安全（只会丢失一次事件循环的写命令），但是性能较差，而everysec模式只不过会可能丢失1秒钟的数据，而no模式的效率和everysec相仿，但是会丢失上次同步AOF文件之后的所有写命令数据。

### 怎么实现Redis的高可用？

要想实现高可用，一台机器肯定是不够的，而redis要保证高可用，有2个可选方案。

#### 主从架构

主从模式是最简单的实现高可用的方案，核心就是主从同步。主从同步的原理如下：

1. slave发送sync命令到master
2. master收到sync之后，执行bgsave，生成RDB全量文件
3. master把slave的写命令记录到缓存
4. bgsave执行完毕之后，发送RDB文件到slave，slave执行
5. master发送缓存中的写命令到slave，slave执行

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAotdopLgAB9KKiah8qqA71ZkztXrn885zDVCyFSnricEAPyWdo9w9lxG6icg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这里我写的这个命令是sync，但是在redis2.8版本之后已经使用psync来替代sync了，原因是sync命令非常消耗系统资源，而psync的效率更高。

#### 哨兵

基于主从方案的缺点还是很明显的，假设master宕机，那么就不能写入数据，那么slave也就失去了作用，整个架构就不可用了，除非你手动切换，主要原因就是因为没有自动故障转移机制。而哨兵(sentinel)的功能比单纯的主从架构全面的多了，它具备自动故障转移、集群监控、消息通知等功能。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAotgVjlOy0uURyKRueXhar16F3ndHj2sQOme4tLbAGwg4Pia5dzcucWhXA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

哨兵可以同时监视多个主从服务器，并且在被监视的master下线时，自动将某个slave提升为master，然后由新的master继续接收命令。整个过程如下：

1. 初始化sentinel，将普通的redis代码替换成sentinel专用代码
2. 初始化masters字典和服务器信息，服务器信息主要保存ip:port，并记录实例的地址和ID
3. 创建和master的两个连接，命令连接和订阅连接，并且订阅sentinel:hello频道
4. 每隔10秒向master发送info命令，获取master和它下面所有slave的当前信息
5. 当发现master有新的slave之后，sentinel和新的slave同样建立两个连接，同时每个10秒发送info命令，更新master信息
6. sentinel每隔1秒向所有服务器发送ping命令，如果某台服务器在配置的响应时间内连续返回无效回复，将会被标记为下线状态
7. 选举出领头sentinel，领头sentinel需要半数以上的sentinel同意
8. 领头sentinel从已下线的的master所有slave中挑选一个，将其转换为master
9. 让所有的slave改为从新的master复制数据
10. 将原来的master设置为新的master的从服务器，当原来master重新回复连接时，就变成了新master的从服务器

sentinel会每隔1秒向所有实例（包括主从服务器和其他sentinel）发送ping命令，并且根据回复判断是否已经下线，这种方式叫做主观下线。当判断为主观下线时，就会向其他监视的sentinel询问，如果超过半数的投票认为已经是下线状态，则会标记为客观下线状态，同时触发故障转移。

### 能说说redis集群的原理吗？

如果说依靠哨兵可以实现redis的高可用，如果还想在支持高并发同时容纳海量的数据，那就需要redis集群。redis集群是redis提供的分布式数据存储方案，集群通过数据分片sharding来进行数据的共享，同时提供复制和故障转移的功能。

#### 节点

一个redis集群由多个节点node组成，而多个node之间通过cluster meet命令来进行连接，节点的握手过程：

1. 节点A收到客户端的cluster meet命令
2. A根据收到的IP地址和端口号，向B发送一条meet消息
3. 节点B收到meet消息返回pong
4. A知道B收到了meet消息，返回一条ping消息，握手成功
5. 最后，节点A将会通过gossip协议把节点B的信息传播给集群中的其他节点，其他节点也将和B进行握手

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAot333fTiaaCA3ibxfwpnZ8VbzLkaM4cxHs23REHZVCibGUF4GdZmLNviaPYQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 槽slot

redis通过集群分片的形式来保存数据，整个集群数据库被分为16384个slot，集群中的每个节点可以处理0-16384个slot，当数据库16384个slot都有节点在处理时，集群处于上线状态，反之只要有一个slot没有得到处理都会处理下线状态。通过cluster addslots命令可以将slot指派给对应节点处理。

slot是一个位数组，数组的长度是16384/8=2048，而数组的每一位用1表示被节点处理，0表示不处理，如图所示的话表示A节点处理0-7的slot。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAot9WNKa3CWnMYFY4JK5MKCOdcUZYmsYtpa7RNofu11gamgcRibyU14y0g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当客户端向节点发送命令，如果刚好找到slot属于当前节点，那么节点就执行命令，反之，则会返回一个MOVED命令到客户端指引客户端转向正确的节点。（MOVED过程是自动的）

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUmIwmSMLlrCHEj66HjJlAotRea7czTYia2kyQric4rkZOdnbfD7U4WHLErfu6BjU4jvTGicGZFLVTaicA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果增加或者移出节点，对于slot的重新分配也是非常方便的，redis提供了工具帮助实现slot的迁移，整个过程是完全在线的，不需要停止服务。

#### 故障转移

如果节点A向节点B发送ping消息，节点B没有在规定的时间内响应pong，那么节点A会标记节点B为pfail疑似下线状态，同时把B的状态通过消息的形式发送给其他节点，如果超过半数以上的节点都标记B为pfail状态，B就会被标记为fail下线状态，此时将会发生故障转移，优先从复制数据较多的从节点选择一个成为主节点，并且接管下线节点的slot，整个过程和哨兵非常类似，都是基于Raft协议做选举。

### 了解Redis事务机制吗？

redis通过MULTI、EXEC、WATCH等命令来实现事务机制，事务执行过程将一系列多个命令按照顺序一次性执行，并且在执行期间，事务不会被中断，也不会去执行客户端的其他请求，直到所有命令执行完毕。事务的执行过程如下：

1. 服务端收到客户端请求，事务以MULTI开始
2. 如果客户端正处于事务状态，则会把事务放入队列同时返回给客户端QUEUED，反之则直接执行这个命令
3. 当收到客户端EXEC命令时，WATCH命令监视整个事务中的key是否有被修改，如果有则返回空回复到客户端表示失败，否则redis会遍历整个事务队列，执行队列中保存的所有命令，最后返回结果给客户端

WATCH的机制本身是一个CAS的机制，被监视的key会被保存到一个链表中，如果某个key被修改，那么REDIS_DIRTY_CAS标志将会被打开，这时服务器会拒绝执行事务。

## 24.Redis官方的高可用性解决方案

### **Redis主从复制的问题**

`Redis` **主从复制** 可将 **主节点** 数据同步给 **从节点**，从节点此时有两个作用：

1. 一旦 **主节点宕机**，**从节点** 作为 **主节点** 的 **备份** 可以随时顶上来。
2. 扩展 **主节点** 的 **读能力**，分担主节点读压力。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxV3LicibPm9kPaiaTj9O9EfPhpps16lfIFSO5BgUDCLHj9G7jSlmq5Gl3Q/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**主从复制** 同时存在以下几个问题：

1. 一旦 **主节点宕机**，**从节点** 晋升成 **主节点**，同时需要修改 **应用方** 的 **主节点地址**，还需要命令所有 **从节点**去 **复制** 新的主节点，整个过程需要 **人工干预**。
2. **主节点** 的 **写能力** 受到 **单机的限制**。
3. **主节点** 的 **存储能力** 受到 **单机的限制**。
4. **原生复制** 的弊端在早期的版本中也会比较突出，比如：`Redis` **复制中断** 后，**从节点** 会发起 `psync`。此时如果 **同步不成功**，则会进行 **全量同步**，**主库** 执行 **全量备份** 的同时，可能会造成毫秒或秒级的 **卡顿**。

### **Redis 的 哨兵（Sentinel）深入探究**

#### **Redis Sentinel的架构**

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxhtzarSZibCBoIR2jXX0mTHq7bn5aenaiboGkGl5rI96TSGwER7qd26Ug/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

Redis的哨兵机制就是解决我们以上主从复制存在缺陷（选举问题），保证我们的Redis高可用，实现自动化故障发现与故障转移。

该系统执行以下三个任务：

> “
>
> 监控：哨兵会不断检查你的主服务器和从服务器是否运作正常。
>
> 提醒：当被监控的某个Redis服务器出现问题时，哨兵可以通过API给程序员发送通知
>
> 自动故障转移：主服务器宕机，哨兵会开始一次自动故障转移操作，升级一个从服务器为主服务器，并让其他从服务器改为复制新的主服务器.

#### **配置 Sentinel**

Redis 源码中包含了一个名为 sentinel.conf 的文件， 这个文件是一个带有详细注释的 Sentinel 配置文件示例。

运行一个 Sentinel 所需的最少配置如下所示：

> “
>
> **`1）sentinel monitor mymaster 192.168.10.202 6379 2`**
>
> ```
> Sentine监听的maste地址，第一个参数是给master起的名字，第二个参数为master IP，第三个为master端口，第四个为当该master挂了的时候，若想将该master判为失效，
> 在Sentine集群中必须至少2个Sentine同意才行，只要该数量不达标，则就不会发生故障迁移。
> -----------------------------------------------------------------------------------------------
> ```
>
> **`2）sentinel down-after-milliseconds mymaster 30000`**
>
> ```
> 表示master被当前sentinel实例认定为失效的间隔时间，在这段时间内一直没有给Sentine返回有效信息，则认定该master主观下线。
> 只有在足够数量的 Sentinel 都将一个服务器标记为主观下线之后， 服务器才会被标记为客观下线，``将服务器标记为客观下线所需的 Sentinel 数量由对主服务器的配置决定。
> -----------------------------------------------------------------------------------------------
> ```
>
> **`3）sentinel parallel-syncs mymaster 2`**
>
> ```
> 当在执行故障转移时，设置几个slave同时进行切换master，该值越大，则可能就有越多的slave在切换master时不可用，可以将该值设置为1，即一个一个来，这样在某个
> slave进行切换master同步数据时，其余的slave还能正常工作，以此保证每次只有一个从服务器处于不能处理命令请求的状态。
> -----------------------------------------------------------------------------------------------
> ```
>
> **`4）sentinel can-failover mymaster ``yes`**
>
> ```
> 在sentinel检测到O_DOWN后，是否对这台redis启动failover机制
> -----------------------------------------------------------------------------------------------
> ```
>
> **`5）sentinel auth-pass mymaster 20180408`**
>
> ```
> 设置sentinel连接的master和slave的密码，这个需要和redis.conf文件中设置的密码一样
> -----------------------------------------------------------------------------------------------
> ```
>
> **`6）sentinel failover-timeout mymaster 180000`**
>
> ```
> failover过期时间，当failover开始后，在此时间内仍然没有触发任何failover操作，当前sentinel将会认为此次failoer失败。 
> 执行故障迁移超时时间，即在指定时间内没有大多数的sentinel 反馈master下线，该故障迁移计划则失效
> -----------------------------------------------------------------------------------------------
> ```
>
> **`7）sentinel config-epoch mymaster 0`**
>
> ```
> 选项指定了在执行故障转移时， 最多可以有多少个从服务器同时对新的主服务器进行同步。这个数字越小， 完成故障转移所需的时间就越长。
> -----------------------------------------------------------------------------------------------
> ```
>
> **`8）sentinel notification-script mymaster ``/var/redis/notify``.sh`**
>
> ```
> 当failover时，可以指定一个``"通知"``脚本用来告知当前集群的情况。
> 脚本被允许执行的最大时间为60秒，如果超时，脚本将会被终止(KILL)
> -----------------------------------------------------------------------------------------------
> ```
>
> **`9）sentinel leader-epoch mymaster 0`**
>
> ```
> 同时一时间最多0个slave可同时更新配置,建议数字不要太大,以免影响正常对外提供服务。
> ```

**主观下线和客观下线**

> “
>
> - 主观下线：指的是单个 Sentinel 实例对服务器做出的下线判断。
> - 客观下线：指的是多个 Sentinel 实例在对同一个服务器做出 SDOWN主观下线 判断。

#### **Redis Sentinel的工作原理**

1.每个 Sentinel 以每秒一次的频率向它所知的主服务器、从服务器以及其他 Sentinel 实例**发送一个 PING 命令**。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1Dxr9OYSyfYtGicMibMP6iaz5anJADxzjsvUURW99drYIJIqicbKaS7YFPxtg/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

2.如果一个实例距离最后一次有效回复 PING 命令的时间超过指定的值， 那么这个实例会被 Sentinel 标记为**主观下线**。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxXwxBKKLqr6Nm8EBLSMp34pSCVuECYtkZibZrcdKs4dT5WQ5YG6HCj6g/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

3.正在监视这个主服务器的所有 Sentinel 要以每秒一次的频率**确认主服务器的确进入了主观下线状态**。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxIy8F2AT153EHicBp78uiazxlkajrhS7mAds4GVCOHoYER5HPFOyUR4wA/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

4.有足够数量的 Sentinel 在指定的时间范围内同意这一判断， 那么这个主服务器被**标记为客观下线**。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxShPbArykCz0bEHQdu9x0Tdo3YRtC6nVwP3e4ShVXkOC2j4TzktkxVw/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

5.每个 Sentinel 会以每 10 秒一次的频率向它已知的所有主服务器和从服务器**发送 INFO 命令**。当一个主服务器被 Sentinel 标记为客观下线时， Sentinel 向下线主服务器的所有从服务器发送 INFO 命令的频率会从 10 秒一次改为每秒一次。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxL7ibiaxiau2Q86libcsuh1uAc8ngxMmibPhThTP6RGQ8Y7NqDLhV8yYn5zA/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

`6.Sentinel` 和其他 `Sentinel` 协商 **主节点** 的状态，如果 **主节点** 处于 `SDOWN` 状态，则投票自动选出新的 **主节点**。将剩余的 **从节点** 指向 **新的主节点** 进行 **数据复制**。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1Dxib31WcfMzGAW5ibHAvuT0EC4eoGhy3o5zVXTXlRmeKGDXUGsbyKa6XkA/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

7.当没有足够数量的 `Sentinel` 同意 **主服务器** 下线时， **主服务器** 的 **客观下线状态** 就会被移除。当 **主服务器** 重新向 `Sentinel` 的 `PING` 命令返回 **有效回复** 时，**主服务器** 的 **主观下线状态** 就会被移除。

![图片](https://mmbiz.qpic.cn/mmbiz/wbiax4xEAl5z1Km7JmrTA0lksv7PFn1DxibNSAOMz2ZCVCGhuLnd30Whqg9AR3FCAfYVr8kb97kQoZAdHLGmRmPg/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### **自动发现 Sentinel 和从服务器**

**一个 Sentinel 可以与其他多个 Sentinel 进行连接， 各个 Sentinel 之间可以互相检查对方的可用性， 并进行信息交换。**

你无须为运行的每个 Sentinel 分别设置其他 Sentinel 的地址， 因为 Sentinel 可以通过**发布与订阅功能**来自动发现正在监视**相同主服务器的其他 Sentinel**。

> “
>
> - 每个 Sentinel 会以每两秒一次的频率， 通过发布与订阅功能， 向被它监视的所有主服务器和从服务器的频道发送一条信息， 信息中包含了 Sentinel 的 IP 地址、端口号和运行 ID （runid）。
> - 每个 Sentinel 都订阅了被它监视的所有主服务器和从服务器的频道， 查找之前未出现过的 sentinel 。当一个 Sentinel 发现一个新的 Sentinel 时， 它会将新的 Sentinel 添加到一个列表中。
> - Sentinel 发送的信息中还包括完整的主服务器当前配置。如果一个 Sentinel 包含的主服务器配置比另一个 Sentinel 发送的配置要旧， 那么这个 Sentinel 会立即升级到新配置上。
> - 在将一个新 Sentinel 添加到监视主服务器的列表上面之前， Sentinel 会先检查列表中是否已经包含了和要添加的 Sentinel 拥有相同运行 ID 或者相同地址（包括 IP 地址和端口号）的 Sentinel ， 如果是的话， Sentinel 会先移除列表中已有的那些拥有相同运行 ID 或者相同地址的 Sentinel ， 然后再添加新 Sentinel

#### **故障转移**

一次故障转移操作由以下步骤组成：

> “
>
> - 发现主服务器已经进入客观下线状态。
> - 对我们的当前纪元进行自增， 并尝试在这个纪元中当选。
> - 如果当选失败， 那么在设定的故障迁移超时时间的两倍之后， 重新尝试当选。如果当选成功， 那么执行以下步骤。
> - 选出一个从服务器，并将它升级为主服务器。
> - 向被选中的从服务器发送 `SLAVEOF NO ONE` 命令，让它转变为主服务器。
> - 通过**发布与订阅功能**， **将更新后的配置传播给所有其他 Sentinel ， 其他 Sentinel 对它们自己的配置进行更新**。
> - 向已下线主服务器的从服务器发送 SLAVEOF 命令， 让它们去复制新的主服务器。
> - 当所有从服务器都已经开始复制新的主服务器时， 领头 Sentinel 终止这次故障迁移操作。

参考：

https://redis.io/

https://www.cnblogs.com/bingshu/p/9776610.html