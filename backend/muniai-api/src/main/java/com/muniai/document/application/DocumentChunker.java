package com.muniai.document.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class DocumentChunker {
    private final IndexingConfigurationProperties properties;
    public DocumentChunker(IndexingConfigurationProperties properties){this.properties=properties;}

    public List<Chunk> chunk(String source){
        String text=normalize(source);
        if(text.isBlank()) return List.of();
        List<Chunk> chunks=new ArrayList<>();
        int start=0,index=0,size=properties.chunkSize(),overlap=properties.chunkOverlap();
        while(start<text.length()){
            int desiredEnd=Math.min(text.length(),start+size);
            int end=boundary(text,start,desiredEnd);
            if(end<=start) end=desiredEnd;
            String content=text.substring(start,end).strip();
            if(!content.isEmpty()) chunks.add(new Chunk(index++,content,sha256(content),start,end,
                    Math.max(1,(content.length()+3)/4)));
            if(end>=text.length()) break;
            int next=Math.max(start+1,end-overlap);
            while(next>start && next<text.length() && !Character.isWhitespace(text.charAt(next-1))) next--;
            start=next>start?next:end;
            while(start<text.length() && Character.isWhitespace(text.charAt(start))) start++;
        }
        return List.copyOf(chunks);
    }
    public String normalize(String value){
        if(value==null) return "";
        String[] paragraphs=value.replace("\r\n","\n").replace('\r','\n').split("\\n\\s*\\n");
        List<String> clean=new ArrayList<>();
        for(String paragraph:paragraphs){
            String normalized=paragraph.replaceAll("[\\t\\n ]+"," ").strip();
            if(!normalized.isEmpty()) clean.add(normalized);
        }
        return String.join("\n\n",clean);
    }
    private int boundary(String text,int start,int desiredEnd){
        if(desiredEnd>=text.length()) return text.length();
        int paragraph=text.lastIndexOf("\n\n",desiredEnd);
        if(paragraph>start+(desiredEnd-start)/2) return paragraph;
        for(int i=desiredEnd;i>start;i--) if(Character.isWhitespace(text.charAt(i-1))) return i-1;
        return desiredEnd;
    }
    private String sha256(String content){
        try{
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        }catch(Exception exception){throw new IllegalStateException("SHA-256 is unavailable.",exception);}
    }
    public record Chunk(int index,String content,String contentHash,int characterStart,int characterEnd,int tokenCountEstimate){}
}
