import { api } from './client'

export type Mood =
  | 'HAPPY'
  | 'SAD'
  | 'ANXIOUS'
  | 'CALM'
  | 'STRESSED'
  | 'ANGRY'
  | 'EXCITED'
  | 'TIRED'
  | 'MOTIVATED'
  | 'NEUTRAL'

export interface JournalEntryResponse {
  id: number
  content: string
  mood: Mood | null
  createdAt: string
}

export interface CreateJournalEntryRequest {
  content: string
  mood?: Mood | null
}

export const journalApi = {
  create: (data: CreateJournalEntryRequest) =>
    api.post<JournalEntryResponse>('/journal', data),

  list: () =>
    api.get<JournalEntryResponse[]>('/journal'),
}
