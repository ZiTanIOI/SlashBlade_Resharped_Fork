# 1.20.1-1.1.24
Merged PR \#83:  
>1.去除了幻影刃类SA的异常多次挥刀伤害（改为一次并上调伤害与ComboA单次伤害一致）
2.为所有SA添加了评分等级伤害增幅，并下调了除虚无刀界外的SA的数值。
3.反弹的投射物会受到重力影响，但增加了初始速度并增加了精准度。（用于修复#55）
4.上调极限锁定距离为40（原12，但在LockOnManager.java中本身瞄准索敌的极限距离是40格）
>
   
>1.removed the abnormal multiple swing damage of the Phantom Blade class SAs (replaced with one and adjusted the damage upwards to match the Combo A single damage)
2.Added scoring level damage increases to all SAs and adjusted values downward for SAs other than Void Blade.
3.bounced projectiles are affected by gravity, but increased initial speed and increased accuracy. (Used to fix \#55)
4.adjusted limit lock distance upwards to 40 (was 12, but in LockOnManager.java itself the limit distance for aiming a solo enemy is 40 blocks)


# 1.20.1-1.1.23
Merged PR \#81:  
>1.修复在攻击时有其他代码同时调用玩家攻击属性时，伤害异常的BUG。  
2.修复配置文件命名错误的bug。  
3.增强受击音效反馈。  
4.修复Give指令/FTB任务给予玩家拔刀会掉落两把刀的BUG  
5.修复幻影剑特殊情况下无法释放且无法使用传送门的BUG  
>
   
>1.Fix wrong damage when some codes try to use player's Attributes.  
2.Fix config file's file name.
3.Improve hit sounds.
4.Fix the bug that drop 2 blades when using give command or FTB request.
5.Fix the bug that summon blade arts can't be used in some case.
>

Fix the advancements' display, now display correct key.