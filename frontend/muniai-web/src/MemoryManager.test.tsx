import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import MemoryManager from './MemoryManager'

const active={id:'m1',category:'GOAL',key:'target_role',value:'AI Engineer',displayLabel:'Target role',status:'ACTIVE',sourceType:'MANUAL_ENTRY',approvedAt:'2026-08-02T00:00:00Z',expiresAt:null,supersededByMemoryId:null,lastUsedAt:null,usageCount:2,createdAt:'2026-08-02T00:00:00Z',updatedAt:'2026-08-02T00:00:00Z'}
describe('MemoryManager',()=>{
 afterEach(()=>{cleanup();vi.restoreAllMocks()})
 it('renders active memories and usage transparency',async()=>{mock(()=>json([active]));render(<MemoryManager/>);expect(await screen.findByText('AI Engineer')).toBeInTheDocument();expect(screen.getByText(/Used 2 times/)).toBeInTheDocument()})
 it('creates a manual proposal without approving it',async()=>{const fetch=mock(({method})=>method==='POST'?json({...active,id:'m2',status:'PROPOSED'}):json([]));render(<MemoryManager/>);await screen.findByText('No active memories.');await userEvent.type(screen.getByLabelText('Memory key'),'target_role');await userEvent.type(screen.getByLabelText('Memory label'),'Target role');await userEvent.type(screen.getByLabelText('Memory value'),'AI Engineer');await userEvent.click(screen.getByRole('button',{name:'Create proposal'}));await waitFor(()=>expect(fetch).toHaveBeenCalledWith('/api/v1/memories/proposals',expect.objectContaining({method:'POST'})))})
 it('shows proposed and disabled empty states',async()=>{mock(()=>json([]));render(<MemoryManager/>);await screen.findByText('No active memories.');await userEvent.click(screen.getByRole('button',{name:'Proposed'}));expect(screen.getByText('No proposed memories.')).toBeInTheDocument();await userEvent.click(screen.getByRole('button',{name:'Disabled'}));expect(screen.getByText('No disabled memories.')).toBeInTheDocument()})
 it('filters and searches memories',async()=>{mock(()=>json([active,{...active,id:'m2',category:'SKILL',key:'skill',value:'Java',displayLabel:'Skill'}]));render(<MemoryManager/>);await screen.findByText('AI Engineer');await userEvent.selectOptions(screen.getByLabelText('Filter memory category'),'SKILL');expect(screen.getByText('Java')).toBeInTheDocument();expect(screen.queryByText('AI Engineer')).not.toBeInTheDocument()})
 it('shows safe API errors',async()=>{mock(()=>new Response(JSON.stringify({message:'Memory unavailable.',correlationId:'ref-memory'}),{status:503,headers:{'Content-Type':'application/json'}}));render(<MemoryManager/>);expect(await screen.findByRole('alert')).toHaveTextContent('Reference: ref-memory')})
})
function mock(handler:(request:{method:string;path:string})=>Response){return vi.spyOn(globalThis,'fetch').mockImplementation((input,init)=>Promise.resolve(handler({method:init?.method??'GET',path:input.toString()})))}
function json(value:unknown){return new Response(JSON.stringify(value),{status:200,headers:{'Content-Type':'application/json'}})}
