Cardboard Box
=============

A simple library for storing and retrieving ItemStacks in Bukkit-related environments.

Example
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

Using
-----

```xml
<repository>
    <id>dependency.download</id>
    <url>https://dependency.download/repository/public/</url>
</repository>
```

```xml
<dependency>
    <groupId>org.kitteh</groupId>
    <artifactId>cardboardbox</artifactId>
    <version>1.0.0</version>
</dependency>
```