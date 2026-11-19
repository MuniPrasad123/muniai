package com.muniai.memory.application;

import com.muniai.memory.domain.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class MemorySelectionService {
    private static final Set<String> ALWAYS=Set.of("preferred_name","response_style","preferred_language","target_role");
    private final MemoryApplicationService memories;
    private final MemoryConfigurationProperties properties;
    public MemorySelectionService(MemoryApplicationService memories,MemoryConfigurationProperties properties){this.memories=memories;this.properties=properties;}
    public List<UserMemory> select(String message){
        if(!properties.enabled())return List.of();
        String q=message.toLowerCase(Locale.ROOT);
        boolean technical=contains(q,"code","java","spring","react","technical","architecture","programming","api");
        boolean career=contains(q,"career","job","role","interview","resume","learn next");
        boolean learning=contains(q,"learn","study","certification","course","explain");
        boolean project=contains(q,"project","muniai","build","application");
        Set<String> keys=new LinkedHashSet<>(ALWAYS);
        if(technical)keys.addAll(Set.of("skill","experience_level","preferred_language"));
        if(career)keys.addAll(Set.of("target_role","job_search_context","current_job_search_focus","experience_level","skill"));
        if(learning)keys.addAll(Set.of("learning_goal","certification_goal","certification","preferred_explanation_style"));
        if(project)keys.add("project");
        List<UserMemory> selected=new ArrayList<>(); int characters=0;
        for(UserMemory memory:memories.active()){
            boolean relevant=keys.contains(memory.key())||(memory.category()==MemoryCategory.PROJECT&&q.contains(memory.value().toLowerCase(Locale.ROOT)));
            if(!relevant)continue;
            int size=memory.key().length()+memory.value().length()+4;
            if(selected.size()>=properties.maxPromptItems()||characters+size>properties.maxPromptCharacters())continue;
            selected.add(memory);characters+=size;
        }
        return List.copyOf(selected);
    }
    private boolean contains(String value,String... terms){return Arrays.stream(terms).anyMatch(value::contains);}
}
