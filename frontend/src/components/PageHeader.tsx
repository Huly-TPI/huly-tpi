interface PageHeaderProps {
    title: string
    subtitle?: string
}

export default function PageHeader({ title, subtitle }: PageHeaderProps) {
    return (
        <div className="mb-6">
            <h1 className="text-3xl font-bold text-bosque">
                {title}
            </h1>
            {subtitle && (
                <p className="text-gray-600 mt-2">{subtitle}</p>
            )}
        </div>
    )
}