import { screen, waitFor } from '@testing-library/react'
import { expect, vi } from 'vitest'
import userEvent from '@testing-library/user-event'
import { api } from '../api/client'

export type User = ReturnType<typeof userEvent.setup>

export async function clickButton(user: User, name: string | RegExp): Promise<void> {
    const button = screen.getByRole('button', { name })
    await user.click(button)
}

export async function clickCheckbox(user: User): Promise<void> {
    const checkbox = screen.getByRole('checkbox')
    await user.click(checkbox)
}

export async function typePlaceholder(user: User, placeholder: string | RegExp, text: string): Promise<void> {
    const input = screen.getByPlaceholderText(placeholder)
    await user.type(input, text)
}

export function verifyTextPresent(text: string | RegExp): void {
    expect(screen.getByText(text)).toBeInTheDocument()
}

export function verifyTextNotPresent(text: string | RegExp): void {
    expect(screen.queryByText(text)).not.toBeInTheDocument()
}

export function verifyPlaceholderPresent(placeholder: string | RegExp): void {
    expect(screen.getByPlaceholderText(placeholder)).toBeInTheDocument()
}

export function verifyButtonDisabled(name: string | RegExp): void {
    expect(screen.getByRole('button', { name })).toBeDisabled()
}

export function verifyButtonEnabled(name: string | RegExp): void {
    expect(screen.getByRole('button', { name })).toBeEnabled()
}

export function verifyButtonPresent(name: string | RegExp): void {
    expect(screen.getByRole('button', { name })).toBeInTheDocument()
}

export function verifyTextPresentAsync(text: string | RegExp): Promise<void> {
    return waitFor(() => {
        expect(screen.getByText(text)).toBeInTheDocument()
    })
}

export function verifyHeadingPresentAsync(name: string | RegExp): Promise<void> {
    return waitFor(() => {
        expect(screen.getByRole('heading', { name })).toBeInTheDocument()
    })
}

export function verifyAlertsCountGreaterThan(count: number): void {
    expect(screen.getAllByRole('alert').length).toBeGreaterThan(count)
}

export function clearAllMocks(): void {
    vi.clearAllMocks()
}

export function verifyHeadingPresent(name: string | RegExp): void {
    expect(screen.getByRole('heading', { name })).toBeInTheDocument()
}

export function verifyDisplayValuePresent(value: string): void {
    expect(screen.getByDisplayValue(value)).toBeInTheDocument()
}

export function verifyValidationAlertsShown(): void {
    verifyAlertsCountGreaterThan(0)
}

export function getLoadingSpinner(): Element | null {
    return document.querySelector('.animate-spin')
}

export const setupMockedGetResponse = (response: any) => {
    vi.mocked(api.get).mockResolvedValueOnce(response)
}

export const setupMockedGetError = (error: any) => {
    vi.mocked(api.get).mockRejectedValueOnce(error)
}

export const setupMockedPostResponse = (response: any) => {
    vi.mocked(api.post).mockResolvedValueOnce(response)
}

export const setupMockedPostError = (error: any) => {
    vi.mocked(api.post).mockRejectedValueOnce(error)
}

export const setupMockedPutResponse = (response: any) => {
    vi.mocked(api.put).mockResolvedValueOnce(response)
}

export const setupMockedPutError = (error: any) => {
    vi.mocked(api.put).mockRejectedValueOnce(error)
}

export const setupMockedPostMultipartResponse = (response: any) => {
    vi.mocked(api.postMultipart).mockResolvedValueOnce(response)
}
