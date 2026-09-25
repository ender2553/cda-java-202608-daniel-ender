package com.example.rpg.service;
import com.example.rpg.domain.Monster; import org.springframework.stereotype.Component; import java.util.*;
/** Concrete factory selected by Spring DI; another implementation can be substituted in tests. */
@Component public final class DefaultMonsterFactory implements MonsterFactory { private final Random random=new Random(); public Monster randomMonster(int level){int n=Math.max(1,level); // Switch expression demonstrates polymorphic construction from one factory boundary.
        return switch(random.nextInt(3)){case 0->new Monster("Goblin Scout",35+n*4,5+n,40+n*10,12+n*3);case 1->new Monster("Cave Spider",45+n*5,7+n,55+n*12,18+n*4);default->new Monster("Stone Troll",70+n*8,10+n*2,90+n*18,35+n*6);};} }
