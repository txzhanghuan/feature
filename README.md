# feature
函数计算引擎

## 概述

对于*元数据*、*指标*、*特征*、*变量* 等等<u>不会导致外部状态变更的计算</u>来说本质都是一种函数，通过入参、API调用、数据库读取，再进行一系列运算加工，生成最终结果。
那么对于一个集合的函数运算，往往会有很多重复的接口调用或者重复调用同一种函数的操作，而这些操作往往是可以被简化。
这个引擎减少了一个集合种的函数重复接口调用和重复的函数调用的次数，并且通过一个非常轻量级的方式嵌入到项目中。

#### 烟囱模式调用
A --依赖--> B --依赖--> C（需计算一次）
D --依赖--> E --依赖--> C（需计算一次）

C调用了2次
#### 拓扑顺序调用
C（先计算C)	--> B，E（再同时计算B，E） --> A，D（最后同时计算A，D）

**通过这个计算引擎，C只调用了1次，并且实现了计算并行化。**

## 用法

>该引擎依赖JDK8，以及SpringBoot框架
>
> 需加入Maven编译参数

      ```Java
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.1</version>
        <configuration>
        <source>8</source>
        <target>8</target>
        <encoding>UTF-8</encoding>
        <!-- 重要 -->
        <compilerArgs>
        <arg>-parameters</arg>
        </compilerArgs>
        </configuration>
      </plugin>
      ```
```

通过标注@Feature的形式，将方法转化为一个函数（仍可以通过方法的形式调用）。
下面代码表示test5依赖test4，test4无入参，可直接计算，当我只需要计算test5的时候，计算引擎会根据函数的依赖关系，生成一个DAG图：
即：test5--依赖-->test4 
那么开始计算的时候，计算引擎会先计算test4的值，再把test4的值放入test5的入参中让其计算。

​```Java
@FeatureComponent
public class Test {
		//       					⬇️代表函数名称
    @Feature(name = "test5")
    //													⬇️ 依赖的函数名称
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
> 标志这个方法是一个函数（方法务必是public），name属性为最后生成该函数的名字（可与方法名不同），入参的类型和参数必须已有的函数相同，不支持同名函数。

> @FeatureComponent
> 作用在类上，标明这个类中有函数需要计算，并且集合了Spring中的@Component注解

---

通过Spring自动注入FeatureEngine
> 若找不到对应的Bean的话，请在启动类上面加上该注解@ComponentScan(basePackages = {"com.github.zh"})

入参 **originDataMap**原始数据 以及 **calcFeatures**待计算的函数名称。
返回一个计算完成的函数Map。
默认使用线程池进行并行计算（确保拓扑顺序），默认线程数为机器的核心数✖️2
>可通过feature.featureThreadPoolSize以及feature.featureThreadPoolMaxSize更改。

> 现版本无法解决循环依赖问题，需在编码时确保，加入在环中任意一个节点的原始数据即可打破循环。

```Java
@Service
public class FeatureService {

    @Autowired
    FeatureEngine featureEngine;

    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures){
        return featureEngine.calc(originDataMap, calcFeatures);
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

#### 自定义计算Bean

通过继承AbstractFeatureBean来自定义计算的Bean。
```Java
public abstract class AbstractFeatureBean implements IFeatureBean {

		//必要
    protected String name;

		//非必要
    protected boolean output;
		
		//若有需要依赖别的Bean，则需要将其他Bean的名称写入
    protected List<String> parents;

		//无需填写，系统会自动生成
    protected List<String> children;
}
//Sample
public class OuterFeatureBean extends AbstractFeatureBean {

    private Function<Object[], Object> fn;

    @Override
    public Object execute(Object[] args) {
        return fn.apply(args);
    }

    public OuterFeatureBean(String name, Function<Object[], Object> fn){
        this.name = name;
        this.fn = fn;
    }
}
```
引擎计算加入外部Bean的方法
```Java
@Service
@Slf4j
public class FeatureService {

    @Autowired
    FeatureEngine featureEngine;

    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures){
        Map<String, OuterFeatureBean> map = new HashMap<>(1);
        map.put("zh", new OuterFeatureBean("zh", (a) -> 1));
        return featureEngine.calcWithOuterFeatureBean(originDataMap, calcFeatures, map);
    }
}
```

#### 本地计算FeatureBean后置处理器

```Java
public interface FeaturePostProcessor{

    @Nullable
    default <T extends AbstractFeatureBean> T postProcessAfterInitializationFeature(T featureBean, String featureBeanName) throws BeansException {
        return featureBean;
    }
}
```
同Spring的 **BeanPostProcessor**

