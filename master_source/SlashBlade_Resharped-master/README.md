# SlashBlade: Resharped  

拔刀剑：重锋 —— 结合两代拔刀剑，为现代Minecraft准备的拔刀剑分支    
SlashBlade: Resharped —— A SlashBlade mod fork for Modern Minecraft.  
![picture](https://s2.loli.net/2024/05/21/w2l63c48nbOMyYi.png)    
***

它回来了。  

岁月变迁、光阴流转，或许你已经从那年青涩稚嫩的剑客，成长为仗剑江湖的游侠。  

或许你有时也会感叹，似乎手中这柄利剑，不像过去那般如意趁手。  

你知道，时间给这柄长剑也留下了难以磨灭的印记。  

你知道，它也曾一次次经历修缮、翻新，甚至回炉重造，为的是还能跟上时代的脚步。  

但是你也不太确定，在它的这段坎坎坷坷的成长之路上，它是否丢失了某些东西。  

以至于当你再次拿起它，你感到既熟悉又陌生；你甚至感觉，这不是当年你认识的它了。  

今天，我们将《拔刀剑：重锋》呈现于此，作为给伴随着拔刀剑成长的各位剑客朋友们最好的礼物。  

* 我们将拔刀剑 2 已知的各项 Bug 尽数修复，将拔刀剑 1 代的一些优秀设计重新移植，并在此基础上进一步丰富拔刀剑的机制；  

* 我们改善了拔刀剑与其他模组的兼容性，也给拔刀剑附属包作者们提供了更便利的条件，这都会让《拔刀剑：重锋》更加适合放在整合包中游玩。  

最后，妖怪之山锻刀铺团队将会对《拔刀剑：重锋》模组进行长期的维护。  

你在游玩的过程中遇到的任何问题，或者对模组的意见和建议，都可以向各位主创反馈，我们会给你及时的答复。  

最后，感谢各位陪伴拔刀剑至今的老玩家们。  

锋刃既成，请君一试！  

---
将该项目导入开发环境

```groovy
repositories {
    maven {
        name 'MMMaven'
        url 'https://raw.github.com/0999312/MMMaven/main/repository'
    }
}

dependencies {
    // Now only 0.6.0 available
    implementation fg.deobf("mods.flammpfeil.slashblade:SlashBlade_Resharped:${slash_blade_version}"
}
```

***

I am the storm that is approaching!!  
Welcome to SlashBlade again.

Still, it is a fan work of Demon Blade, DMC & PSO2, which aims to add the DMC-ish KATANA in the game.  

The major differences between this mod to the original SlashBlade are:

* We fixed most of issues we have known.  
>Due to its under active maintainance, if you find something wrong, report it to us and it will be fixed asap.  

* We brought back some features that existed in old versions of SlashBlade but removed later.   
* Better mod integrations:  
> * Correct JEI Recipe showing.  
> * Data-driven Custom Slashblade, which makes SlashBlade Resharpened better in modpacks.  


Take your blade again, and enjoy the combo attack!

Optional library :   
* playerAnimator  Add Support 1.20.1 v0.0.8 or later    

License: MIT License  
