Cardboard Box
=============

A simple library for storing and retrieving ItemStacks in Bukkit-related environments.

Version support
------

Currently, Cardboard Box supports 1.7 through 1.21.6.  
Requires Java 21.  

Will it need an update for 1.21.7? Maybe.  
Will it need an update for 1.22? Definitely.

Support
------
Via [Discord](https://discord.gg/NhxASEPk). #cardboardbox channel on MOSS.

Example Code
-------

Example demonstration of storing and retrieving the item in a player's hand:
```java
if (CardboardBox.isReady()) {
    ItemStack item = player.getInventory().getItemInMainHand();
    byte[] data = CardboardBox.serializeItem(item);
    this.getLogger().info(Base64.getEncoder().encodeToString(data));
    ItemStack item2 = CardboardBox.deserializeItem(data);
    this.getLogger().info(item2.toString());
}
```

Adding to your project
-----

### Repository

```kotlin
maven {
    name = "dependencyDownload"
    url = uri("https://dependency.download/releases")
}
```
```xml
<repository>
    <id>dependency.download</id>
    <url>https://dependency.download/releases</url>
</repository>
```

### Dependency


```kotlin
implementation("dev.kitteh:cardboardbox:3.0.4")
```
```xml
<dependency>
    <groupId>dev.kitteh</groupId>
    <artifactId>cardboardbox</artifactId>
    <version>3.0.4</version>
</dependency>
```
