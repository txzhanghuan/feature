# feature
DAG图计算引擎

## 概述

对于很多元数据（某种程度上来说是一种函数），计算需要入参和API调用，然后再进行一些计算，便可以将这个元数据加工出来。
那么对于一个集合的元数据运算，往往会有很多重复的接口调用或者重复调用同一个元数据的操作，而这些操作往往是可以被简化。
这个引擎减少了一个集合种的元数据重复接口调用和重复的元数据调用的次数，并且通过一个非常轻量级的方式嵌入到项目中。

### 对于旧的元数据调用方式
烟囱模式调用
A --依赖--> B --依赖--> C（需计算一次）
D --依赖--> E --依赖--> C（需计算一次）

C调用了2次

### 对于新的元数据调用方式
拓扑顺序调用
C（先计算C)	--> B，E（再同时计算B，E） --> A，D（最后同时计算A，D）

通过这个计算引擎，C只调用了1次，并且实现了计算并行化。

## 用法

通过标注@Feature的形式，将方法转化为一个元数据（仍可以通过方法的形式调用）。
下面代码表示test5依赖test4，test4无入参，可直接计算，当我只需要计算test5的时候，计算引擎会根据元数据的依赖关系，生成一个DAG图：
即：test5--依赖-->test4 
那么开始计算的时候，计算引擎会先计算test4的值，再把test4的值放入test5的入参中让其计算。
```Java
@FeatureComponent
public class Test {
		//       					⬇️代表元数据名称
    @Feature(name = "test5")
    //													⬇️ 依赖的元数据名称
    public Double test5(Double test4){
        return test4 + 1.0;
    }
    
    @Feature(name = "test4")
    public Double test4(){
        return 1.0;
    }
}
```
> @Feature
> 标志这个方法是一个元数据（方法务必是public），name属性为最后生成该元数据的名字（可与方法名不同），入参的类型和参数必须已有的元数据相同，不支持同名元数据。

> @FeatureComponent
> 作用在类上，标明这个类中有元数据需要计算，并且集合了Spring中的@Component注解

---

通过Spring自动注入FeatureEngine
*若找不到对应的Bean的话，请在启动类上面加上该注解@ComponentScan(basePackages = {"com.github.zh"})*
入参 **originDataMap**原始数据 以及 **calcFeatures**待计算的元数据名称。
返回一个计算完成的元数据Map。
默认使用线程池进行并行计算（确保拓扑顺序），线程数为机器的核心数✖️2，可通过feature.featureThreadPoolSize更改。
现版本无法解决循环依赖问题，需在编码时确保，加入原始数据可打破环。

```Java
@Service
@Slf4j
public class FeatureService {

    @Autowired
    FeatureEngine featureEngine;

    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures){
        return featureEngine.calc(originDataMap, calcFeatures);
    }

    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures){
        Map<String, OuterFeatureBean> map = new HashMap<>(1);
        map.put("zh", new OuterFeatureBean("zh", (a) -> 1));
        return featureEngine.calcWithOuterFeatureBean(originDataMap, calcFeatures, map);
    }
}
```
输入样例：
```json
{
  "calcFeatures": [
    "test5"
  ],
  "originDataMap": {
  }
}
```
输出样例：
```json
{
    "test4": 1,
    "test5": 2
  }
```
## 进阶
