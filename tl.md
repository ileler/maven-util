这个东西其实我做了很久了，但一直没共享出来，因为总感觉这个也不是很完美，，，
但平常工作时还是总有人因为项目启不来而浪费很多时间，有看得起我的同事也会来找我帮他们解决，
但我每次都是不情愿的/笑哭，因为我早就不用他们那套启项目的方式了，觉得太折腾人了，实在不想浪费时间。。。。今天抽空将这个小工具分享给大家，，，
不喜勿喷，有更高效率的工具或方法，还请大家不吝赐教，，，，  
最开始来公司第一次接触virgo时就被狠狠折腾了一段时间，，，每次新拉个项目第一次启动总要费好多时间，等项目起来了整个人的干劲都被折腾没了/笑哭，
好不容易项目起来了，再 `新建个bundle` 或是 `新增个java文件` 或是 `新添加个依赖包` 时需要 *重新打包重新部署后* 又TM启不来的有木有/笑哭，，，每次报的错误也无非就是
找不到bundle，版本依赖对不上，每次都要删maven仓库，删virgo的work等等等，其实项目都是没任何问题的，花那么多时间来启动个没问题的项目意义何在？？？，，#￥%……@&\*！
受不了受不了受不了。。。但后面我开始自己上开发环境部署项目时发现，在本地遇到的这些问题，貌似在服务器上根本就一次都没碰到过，
在服务器上遇到的问题那都是真正的问题，，，，这个时候我就在想，为什么在服务器上都没问题，而在本地却这么费劲呢？？？？  
**原来，服务器上启项目就是发布一个plan到virgo的pickup下，然后把项目的jar包和依赖包全部copy到repository/usr下**，这样启动就没任何问题，，，，  
**但是本地是通过eclipse的virgo插件把plan发布到virgo实际是只是从maven上把项目的jar包解压到virgo的stage下**，，，问题就全部出现在这个环节上，  
按我的理解，这个锅要eclipse的virgo插件来背。。。   
于是我就在想，我干脆不用它了，，，我就干脆仿照服务器的发布方式，，，通过maven的方式把jar包直接copy到repository/usr下，然后把plan发布到pickup上，这样做了以后，我就很少碰到项目启不来的问题了，，，，，   
恩恩，于是我就写了个工具类，使用方式如下：   
首先把 **maven-util.jar** 添加到 **CLASSPATH** 下，然后重启eclipse    
![](https://github.com/ileler/maven-util/blob/master/images/3.png)  
然后把项目的 **build/pom.xml** 复制到项目的根目录下重命名为 **deploy.xml** (eg:`org.isli.irms.build/pom.xml`)  
![](https://github.com/ileler/maven-util/blob/master/images/1.png)  
![](https://github.com/ileler/maven-util/blob/master/images/2.png)  
```
|org.isli.irms
|----|org.isli.irms.dba
|----|org.isli.irms.host
|----|org.isli.irms.****
|----|org.isli.irms_1.0.SNAPSHOT.plan
|----|pom.xml
|----|deploy.xml
```
然后在把deploy.xml里面的下面这段
```xml
		<profile>
			<id>dev</id>
				<build>
				<plugins>

					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-dependency-plugin</artifactId>
						<executions>
							<execution>
								<id>copy</id>
								<phase>package</phase>
								<goals>
									<goal>copy-dependencies</goal>
								</goals>
								<configuration>
									<outputDirectory>${project.build.directory}/bundles</outputDirectory>
								</configuration>
							</execution>
						</executions>
					</plugin>
					
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-assembly-plugin</artifactId>
						<version>2.2.1</version>
						<configuration>
							<finalName>mpr</finalName>
							<filters>
								<filter>src/main/resources/filter/dev/filter.properties</filter>
							</filters>
						</configuration>
						<executions>
							<execution>
								<id>config</id>
								<phase>package</phase>
								<goals>
									<goal>single</goal>
								</goals>
								<configuration>
									<skipAssembly>false</skipAssembly>
									<descriptor>src/main/assembly/config.xml</descriptor>
								</configuration>
							</execution>
						</executions>
					</plugin>
				</plugins>
			</build>
		</profile>
```
***更换为***
```xml
    <profile>
			<id>dev</id>
				<build>
				<plugins>
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-dependency-plugin</artifactId>
						<executions>
							<execution>
								<id>copy</id>
								<phase>package</phase>
								<goals>
									<goal>copy-dependencies</goal>
								</goals>
								<configuration>
									<outputDirectory>${virgo.home}\repository\usr</outputDirectory>
									<overWriteReleases>false</overWriteReleases>
              						<overWriteSnapshots>true</overWriteSnapshots>
									<overWriteIfNewer>true</overWriteIfNewer>
								</configuration>
							</execution>
						</executions>
					</plugin>
					<plugin>
						<artifactId>exec-maven-plugin</artifactId>
						<groupId>org.codehaus.mojo</groupId>
						<executions>
							<execution>
								<id>clean</id>
								<phase>clean</phase>
								<goals>
									 <goal>exec</goal>
								</goals>
								<configuration>  
							        <executable>java</executable>
							        <arguments>  
							            <argument>com.ileler.maven.util.MavenUtil</argument>
										<argument>clean</argument>
							            <argument>${virgo.home}</argument>
							        </arguments>  
							    </configuration>
							</execution>
							<execution>
								<id>package</id>
								<phase>package</phase>
								<goals>
									 <goal>exec</goal>
								</goals>
								<configuration>  
							        <executable>java</executable>
							        <arguments>  
							            <argument>com.ileler.maven.util.MavenUtil</argument>
										<argument>deploy</argument>
							            <argument>${basedir}</argument>
							            <argument>${virgo.home}</argument>
							        </arguments>  
							    </configuration>
							</execution>
						</executions>
					</plugin>
				</plugins>
			</build>
		</profile>
```
然后在deploy.xml的 *properties* 节点下添加配置项
`<virgo.home>E:\***\virgo-tomcat-server-***</virgo.home>`也就是你的virgo路径
然后在项目上右击 *Run as* 选择 *Run Configurations...* 然后在 *Maven Build* 下新建一项  **Base directory** 就是项目的路径， **Goals** 就填 `-f ./deploy.xml clean package -Pdev`   
![](https://github.com/ileler/maven-util/blob/master/images/4.png)    
保存后，以后启项目的就只用在项目上右击 *Run as* 选择 *Maven build* 然后就选择你刚刚新增的项，带发布完成后就可以直接启动项目了。。。  
![](https://github.com/ileler/maven-util/blob/master/images/5.png)    
![](https://github.com/ileler/maven-util/blob/master/images/6.png)      
请注意最后一张图， virgo 并没有发布任何 plan ，，，因为发布流程已经不走它了，，，现在只用它启动 virgo 就好了。。。。
