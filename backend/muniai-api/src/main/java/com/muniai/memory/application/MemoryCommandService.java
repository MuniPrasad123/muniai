package com.muniai.memory.application;

import com.muniai.memory.domain.*;
import java.util.*;
import java.util.regex.*;
import org.springframework.stereotype.Service;

@Service
public class MemoryCommandService {
    private static final Pattern KEY_VALUE=Pattern.compile("(?i)^(?:remember(?: that)?|save|update)\\s+(?:my\\s+)?([a-z][a-z _-]{1,60})\\s+(?:is|to|as|=)\\s+(.+?)[.!]?$" );
    private static final Pattern JAVA_DEV=Pattern.compile("(?i)^remember that i am an? (.+?) developer[.!]?$" );
    private final MemoryApplicationService memories;
    public MemoryCommandService(MemoryApplicationService memories){this.memories=memories;}
    public Optional<UserMemory> propose(String text,UUID conversationId,UUID messageId){
        Matcher developer=JAVA_DEV.matcher(text.trim());
        if(developer.matches())return Optional.of(memories.propose(MemoryCategory.SKILL,"skill",developer.group(1).trim(),"Skill",MemorySourceType.EXPLICIT_USER_REQUEST,conversationId,messageId,1.0,null));
        Matcher match=KEY_VALUE.matcher(text.trim()); if(!match.matches())return Optional.empty();
        String key=MemoryApplicationService.normalizeKey(match.group(1)); String value=match.group(2).trim();
        return Optional.of(memories.propose(category(key),key,value,label(key),MemorySourceType.EXPLICIT_USER_REQUEST,conversationId,messageId,1.0,null));
    }
    public boolean isRecall(String text){String q=text.trim().toLowerCase(Locale.ROOT);return q.equals("what do you remember about me?")||q.equals("show my saved memories.")||q.equals("show my saved memories");}
    public List<UserMemory> forgetMatches(String text){
        String q=text.trim().toLowerCase(Locale.ROOT);
        if(!(q.startsWith("forget ")||q.startsWith("disable ")))return List.of();
        String target=q.replaceFirst("^(forget|disable)\\s+(my\\s+)?","").replaceAll("\\s+memory[.!]?$","").replaceAll("[.!]$","").trim();
        return memories.active().stream().filter(m->m.key().replace('_',' ').contains(target)||target.contains(m.key().replace('_',' '))||m.value().toLowerCase(Locale.ROOT).contains(target)).toList();
    }
    private MemoryCategory category(String key){
        if(Set.of("preferred_name","location","experience_level").contains(key))return MemoryCategory.PROFILE;
        if(key.startsWith("preferred_")||key.equals("response_style"))return MemoryCategory.PREFERENCE;
        if(key.contains("goal")||key.equals("target_role")||key.equals("certification"))return MemoryCategory.GOAL;
        if(key.equals("skill"))return MemoryCategory.SKILL;if(key.equals("project"))return MemoryCategory.PROJECT;
        if(key.contains("context")||key.startsWith("current_"))return MemoryCategory.CONTEXT;return MemoryCategory.CUSTOM;
    }
    private String label(String key){String[] words=key.split("_");StringBuilder out=new StringBuilder();for(String word:words){if(!out.isEmpty())out.append(' ');out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}return out.toString();}
}
