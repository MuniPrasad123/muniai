export type MemoryCategory = 'PROFILE'|'PREFERENCE'|'GOAL'|'SKILL'|'PROJECT'|'CONTEXT'|'CUSTOM'
export type MemoryStatus = 'PROPOSED'|'ACTIVE'|'DISABLED'|'SUPERSEDED'|'REJECTED'|'DELETED'
export type MemorySource = 'EXPLICIT_USER_REQUEST'|'ASSISTANT_SUGGESTION'|'MANUAL_ENTRY'
export type Memory = { id:string;category:MemoryCategory;key:string;value:string;displayLabel:string;status:MemoryStatus;
  sourceType:MemorySource;approvedAt:string|null;expiresAt:string|null;supersededByMemoryId:string|null;
  lastUsedAt:string|null;usageCount:number;createdAt:string;updatedAt:string }
export class MemoryApiError extends Error { constructor(message:string,readonly status:number,readonly code?:string){super(message);this.name='MemoryApiError'} }
async function request<T>(path:string,init?:RequestInit):Promise<T>{const response=await fetch(path,{...init,headers:{'Content-Type':'application/json','X-Correlation-ID':crypto.randomUUID(),...init?.headers}});if(!response.ok){const body=await response.json().catch(()=>({}));throw new MemoryApiError(`${body.message??'Memory operation failed.'}${body.correlationId?` Reference: ${body.correlationId}`:''}`,response.status,body.code)}if(response.status===204)return undefined as T;return response.json()}
export const memoryApi={
  list:()=>request<Memory[]>('/api/v1/memories'),
  propose:(value:{category:MemoryCategory;key:string;value:string;displayLabel:string})=>request<Memory>('/api/v1/memories/proposals',{method:'POST',body:JSON.stringify({...value,sourceType:'MANUAL_ENTRY'})}),
  edit:(id:string,value:{category:MemoryCategory;key:string;value:string;displayLabel:string;expiresAt?:string|null})=>request<Memory>(`/api/v1/memories/${id}`,{method:'PATCH',body:JSON.stringify(value)}),
  action:(id:string,action:'approve'|'reject'|'disable'|'enable')=>request<Memory>(`/api/v1/memories/${id}/${action}`,{method:'POST'}),
  supersede:(oldId:string,replacementMemoryId:string)=>request<Memory>(`/api/v1/memories/${oldId}/supersede`,{method:'POST',body:JSON.stringify({replacementMemoryId})}),
  remove:(id:string)=>request<void>(`/api/v1/memories/${id}`,{method:'DELETE'}),
  removeAll:()=>request<void>('/api/v1/memories',{method:'DELETE'}),
}
