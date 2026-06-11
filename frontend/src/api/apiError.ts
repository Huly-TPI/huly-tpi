export class ApiError extends Error {
    fieldErrors: Record<string, string>
    status?: number

    constructor(message: string, fieldErrors: Record<string, string> = {}, status?: number) {
        super(message)
        this.name = 'ApiError'
        this.fieldErrors = fieldErrors
        this.status = status
    }
}