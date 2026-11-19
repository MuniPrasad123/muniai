import { FormEvent, useCallback, useEffect, useState } from 'react'
import { Memory, MemoryApiError, MemoryCategory, memoryApi } from './api/memories'
const categories:MemoryCategory[]=['PROFILE','PREFERENCE','GOAL','SKILL','PROJECT','CONTEXT','CUSTOM']
export default function MemoryManager(){
  const [items,setItems]=useState<Memory[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState('')
  const [status,setStatus]=useState<'ACTIVE'|'PROPOSED'|'DISABLED'>('ACTIVE'),[category,setCategory]=useState<MemoryCategory|''>(''),[search,setSearch]=useState('')
  const [form,setForm]=useState({category:'PROFILE' as MemoryCategory,key:'',value:'',displayLabel:''})
  const load=useCallback(async()=>{setLoading(true);try{setItems(await memoryApi.list());setError('')}catch(e){setError(messageOf(e))}finally{setLoading(false)}},[])
  useEffect(()=>{void load()},[load])
  async function create(e:FormEvent){e.preventDefault();try{await memoryApi.propose(form);setForm({...form,key:'',value:'',displayLabel:''});await load()}catch(x){setError(messageOf(x))}}
  async function action(memory:Memory,name:'approve'|'reject'|'disable'|'enable'){
    try{await memoryApi.action(memory.id,name);await load()}catch(x){
      if(name==='approve'&&x instanceof MemoryApiError&&x.code==='MEMORY_CONFLICT'){
        const old=items.find(v=>v.status==='ACTIVE'&&v.key===memory.key)
        if(old&&window.confirm(`Replace ${old.displayLabel}: ${old.value} with ${memory.value}?`)){await memoryApi.supersede(old.id,memory.id);await load();return}
      }setError(messageOf(x))
    }
  }
  async function edit(memory:Memory){const value=window.prompt(`Edit ${memory.displayLabel}`,memory.value)?.trim();if(!value)return;try{await memoryApi.edit(memory.id,{category:memory.category,key:memory.key,value,displayLabel:memory.displayLabel,expiresAt:memory.expiresAt});await load()}catch(x){setError(messageOf(x))}}
  async function remove(memory:Memory){if(!window.confirm(`Delete ${memory.displayLabel}? History will retain a deleted marker.`))return;try{await memoryApi.remove(memory.id);await load()}catch(x){setError(messageOf(x))}}
  const visible=items.filter(v=>v.status===status&&(!category||v.category===category)&&(!search||`${v.displayLabel} ${v.key} ${v.value}`.toLowerCase().includes(search.toLowerCase())))
  return <section className="memory-page" aria-labelledby="memory-title"><header><p className="eyebrow">LOCAL · USER CONTROLLED</p><h1 id="memory-title">Memory</h1><p>Only memories you explicitly approve can personalize future conversations.</p></header>
    {error&&<div role="alert" className="error">{error}</div>}
    <form className="memory-form" onSubmit={create}><h2>Add memory proposal</h2><select aria-label="Memory category" value={form.category} onChange={e=>setForm({...form,category:e.target.value as MemoryCategory})}>{categories.map(v=><option key={v}>{v}</option>)}</select>
      <input aria-label="Memory key" placeholder="target_role" value={form.key} onChange={e=>setForm({...form,key:e.target.value})}/><input aria-label="Memory label" placeholder="Target role" value={form.displayLabel} onChange={e=>setForm({...form,displayLabel:e.target.value})}/>
      <textarea aria-label="Memory value" placeholder="AI Engineer" value={form.value} onChange={e=>setForm({...form,value:e.target.value})}/><button disabled={!form.key.trim()||!form.value.trim()||!form.displayLabel.trim()}>Create proposal</button></form>
    <div className="memory-toolbar">{(['ACTIVE','PROPOSED','DISABLED'] as const).map(v=><button className={status===v?'active':''} key={v} onClick={()=>setStatus(v)}>{v[0]+v.slice(1).toLowerCase()}</button>)}
      <select aria-label="Filter memory category" value={category} onChange={e=>setCategory(e.target.value as MemoryCategory|'')}><option value="">All categories</option>{categories.map(v=><option key={v}>{v}</option>)}</select><input aria-label="Search memories" placeholder="Search" value={search} onChange={e=>setSearch(e.target.value)}/></div>
    {loading?<p>Loading memories…</p>:visible.length===0?<p className="empty-memory">No {status.toLowerCase()} memories.</p>:<div className="memory-list">{visible.map(memory=><article key={memory.id}><span>{memory.category}</span><h3>{memory.displayLabel}</h3><p>{memory.value}</p><small>Source: {memory.sourceType.replaceAll('_',' ')} · Created {new Date(memory.createdAt).toLocaleDateString()} · Used {memory.usageCount} times{memory.lastUsedAt?` · Last ${new Date(memory.lastUsedAt).toLocaleDateString()}`:''}</small><div>
      {memory.status==='PROPOSED'&&<><button onClick={()=>action(memory,'approve')}>Approve</button><button onClick={()=>action(memory,'reject')}>Reject</button></>}{memory.status==='ACTIVE'&&<button onClick={()=>action(memory,'disable')}>Disable</button>}{memory.status==='DISABLED'&&<button onClick={()=>action(memory,'enable')}>Enable</button>}<button onClick={()=>edit(memory)}>Edit</button><button onClick={()=>remove(memory)}>Delete</button></div></article>)}</div>}
    <button className="delete-all" disabled={!items.some(v=>v.status!=='DELETED')} onClick={async()=>{if(window.confirm('Delete all memories? This removes all active personalization.')){await memoryApi.removeAll();await load()}}}>Delete all memories</button>
  </section>
}
function messageOf(value:unknown){return value instanceof Error?value.message:'Memory operation failed.'}
