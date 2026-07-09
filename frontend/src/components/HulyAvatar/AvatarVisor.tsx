import React from 'react';
import { AVATAR_COLORS } from './AvatarConstants';

export type VisorConfig = {
    light: string;
    shadow: string;
    stroke: string;
};

export const getVisorConfig = (assetKey?: string): VisorConfig | null => {
    switch (assetKey) {
        case 'vicera-azul':
            return { light: AVATAR_COLORS.shoeBlueBase, shadow: AVATAR_COLORS.shoeBlueShadow, stroke: AVATAR_COLORS.shoeBlueStroke };
        case 'vicera-rosa':
            return { light: AVATAR_COLORS.visorPinkLight, shadow: AVATAR_COLORS.visorPinkShadow, stroke: AVATAR_COLORS.visorPinkStroke };
        case 'vicera-cafe':
            return { light: AVATAR_COLORS.visorBrownLight, shadow: AVATAR_COLORS.visorBrownShadow, stroke: AVATAR_COLORS.visorBrownStroke };
        default:
            return null;
    }
};

interface AvatarVisorProps {
    assetKey?: string;
}

export const AvatarVisor: React.FC<AvatarVisorProps> = ({ assetKey }) => {
    const config = getVisorConfig(assetKey);

    if (!config) return null;

    const grad56Id = `grad56-${assetKey}`;
    const grad72Id = `grad72-${assetKey}`;
    const grad74Id = `grad74-${assetKey}`;

    return (
        <g id={`vicera-${assetKey}`} strokeWidth="2">
            <defs>
                <radialGradient
                    id={grad56Id}
                    cx="177.18"
                    cy="8.712"
                    r="17.725"
                    gradientTransform="matrix(-.055449 .67746 -.97904 -.080133 205.63 -109.86)"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop stopColor={config.light} offset=".9999" />
                    <stop stopColor={config.shadow} offset=".9999" />
                </radialGradient>
                <radialGradient
                    id={grad72Id}
                    cx="26.941"
                    cy="-30.761"
                    r="103.81"
                    gradientTransform="matrix(-1.0705 -1.5752 1.8399 -1.2504 297.39 90.655)"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop stopColor={config.light} offset=".45113" />
                    <stop stopColor={AVATAR_COLORS.mouthDark} offset=".45113" />
                    <stop stopColor={config.shadow} offset=".45113" />
                </radialGradient>
                <radialGradient
                    id={grad74Id}
                    cx="263.61"
                    cy="60.532"
                    r="101.84"
                    gradientTransform="matrix(1.6385 -.57244 .38093 1.0904 -194.29 140.97)"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop stopColor={config.light} offset=".58356" />
                    <stop stopColor={config.shadow} offset=".58356" />
                </radialGradient>
            </defs>
            <ellipse
                id="vicera-superior"
                cx="179.26"
                cy="9.0714"
                rx="16.725"
                ry="12.095"
                fill={`url(#${grad56Id})`}
                stroke={config.stroke}
            />
            <path
                id="vicera-sombra"
                d="m357.19 105.46s5.5589 11.847-16.364 13.926l-12.549-14.547-15.918-14.641 32.546 2.033z"
                fill={config.shadow}
                stroke={config.shadow}
            />
            <path
                id="vicera-delantera"
                d="m155.73 116.04s29.671 27.214 70.493 27.781c0 0 20.789 5.1027 51.216-20.033 0 0 19.088-18.332 41.199-27.781 0 0 26.458-7.7485 38.365 9.0714 0 0 2.6458-14.552-56.696-31.75l-85.234-26.647z"
                fill={`url(#${grad74Id})`}
                stroke={config.stroke}
            />
            <path
                id="vicera-central"
                d="m178.71 7.2288c-0.76119 1.6742-40.956 23.506-26.387 99.738m-54.051 30.427s47.625-16.064 57.452-21.545l144.58-41.766s-13.229-38.176-42.333-54.996c0 0-24.946-20.6-79.375-11.906 0 0-41.199 9.4494-66.713 41.955 0 0-25.891 32.128-13.607 88.257z"
                fill={`url(#${grad72Id})`}
                stroke={config.stroke}
            />
        </g>
    );
};
