export class ApiError extends Error {
    fieldErrors: Record<string, string>

    constructor(message: string, fieldErrors: Record<string, string> = {}) {
        super(message)
        this.name = 'ApiError'
        this.fieldErrors = fieldErrors
    }
}