package com.muniai.chat.application;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
import com.muniai.ai.application.LanguageModelProvider; import com.muniai.ai.domain.*; import com.muniai.shared.exception.MessageTooLongException;
import org.junit.jupiter.api.Test;
class ChatApplicationServiceTest {
 @Test void returnsSuccessfulCompletion(){LanguageModelProvider p=mock(LanguageModelProvider.class); when(p.complete(any())).thenReturn(new ChatCompletion("answer","model","ollama")); ChatCompletion result=new ChatApplicationService(p,new ChatConfigurationProperties(20)).chat("question"); assertEquals("answer",result.answer()); verify(p).complete(new ChatCompletionRequest("question"));}
 @Test void rejectsMessageOverMaximum(){ChatApplicationService s=new ChatApplicationService(mock(LanguageModelProvider.class),new ChatConfigurationProperties(3)); assertThrows(MessageTooLongException.class,()->s.chat("four"));}
}
