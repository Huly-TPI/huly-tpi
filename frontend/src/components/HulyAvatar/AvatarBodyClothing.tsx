import React from 'react';
import { AVATAR_COLORS } from './AvatarConstants';

export type BodyClothingConfig = {
    type: 'remera' | 'jardinero';
    base: string;
    shadow: string;
    stroke: string;
    pocket?: string;
};

export const getBodyClothingConfig = (assetKey?: string): BodyClothingConfig | null => {
    switch (assetKey) {
        case 'remera-violeta':
            return { type: 'remera', base: AVATAR_COLORS.shirtVioletBase, shadow: AVATAR_COLORS.shirtVioletShadow, stroke: AVATAR_COLORS.shirtVioletStroke };
        case 'remera-rosa':
            return { type: 'remera', base: AVATAR_COLORS.shirtPinkBase, shadow: AVATAR_COLORS.shirtPinkShadow, stroke: AVATAR_COLORS.shirtPinkStroke };
        case 'remera-azul':
            return { type: 'remera', base: AVATAR_COLORS.shirtBlueBase, shadow: AVATAR_COLORS.shirtBlueShadow, stroke: AVATAR_COLORS.shirtBlueStroke };
        case 'remera-mostaza':
            return { type: 'remera', base: AVATAR_COLORS.mustardBase, shadow: AVATAR_COLORS.mustardShadow, stroke: AVATAR_COLORS.mustardStroke };
        case 'remera-turquesa':
            return { type: 'remera', base: AVATAR_COLORS.tealBase, shadow: AVATAR_COLORS.tealShadow, stroke: AVATAR_COLORS.tealStroke };
        case 'remera-carmesi':
            return { type: 'remera', base: AVATAR_COLORS.crimsonBase, shadow: AVATAR_COLORS.crimsonShadow, stroke: AVATAR_COLORS.crimsonStroke };
        case 'remera-lavanda':
            return { type: 'remera', base: AVATAR_COLORS.lavenderBase, shadow: AVATAR_COLORS.lavenderShadow, stroke: AVATAR_COLORS.lavenderStroke };
        case 'remera-carbon':
            return { type: 'remera', base: AVATAR_COLORS.charcoalBase, shadow: AVATAR_COLORS.charcoalShadow, stroke: AVATAR_COLORS.charcoalStroke };
        
        case 'jardinero-rosa':
            return { type: 'jardinero', base: AVATAR_COLORS.overallsPinkBase, shadow: AVATAR_COLORS.overallsPinkShadow, stroke: AVATAR_COLORS.overallsPinkBase };
        case 'jardinero-gris':
            return { type: 'jardinero', base: AVATAR_COLORS.overallsGreyBase, shadow: AVATAR_COLORS.overallsGreyShadow, stroke: AVATAR_COLORS.overallsGreyBase, pocket: AVATAR_COLORS.white };
        case 'jardinero-mostaza':
            return { type: 'jardinero', base: AVATAR_COLORS.mustardBase, shadow: AVATAR_COLORS.mustardShadow, stroke: AVATAR_COLORS.mustardBase };
        case 'jardinero-carbon':
            return { type: 'jardinero', base: AVATAR_COLORS.charcoalBase, shadow: AVATAR_COLORS.charcoalShadow, stroke: AVATAR_COLORS.charcoalBase, pocket: AVATAR_COLORS.white };
        default:
            return null;
    }
};

interface AvatarBodyClothingProps {
    assetKey?: string;
    part: 'right-arm' | 'left-arm' | 'torso';
}

export const AvatarBodyClothing: React.FC<AvatarBodyClothingProps> = ({ assetKey, part }) => {
    const config = getBodyClothingConfig(assetKey);

    // Si no hay configuracion, y nos piden el torso, dibujamos el jardinero default
    if (!config) {
        if (part === 'torso') {
            return (
                <g id="g15-default">
                    <path
                        id="jardinero-default"
                        d="m205.8 325.72s17.212 3.9288 12.348 23.573c0 0-0.74835 29.186-24.322 33.863 0 0-0.37418 0.37418-1.8709 5.7998 0 0 20.206 0.46773 23.199 6.5481 2.9934 6.0804-18.02 37.025-5.0139 44.023 22.054 11.865 50.061 16.881 86.21 3.3106 0 0 10.243-44.597-27.362-116.44 0 0-5.096-2.9262-5.6281 1.3252-0.52059 4.1597 5.8682 17.03 0.91737 22.808-3.7752 4.4056-11.853 3.9891-17.293 1.9733-10.175-3.7701-12.543-18.792-22.13-23.874-5.6771-3.0093-16.048-2.3743-19.055-2.91z"
                        fill={AVATAR_COLORS.overallsBase}
                    />
                    <path
                        id="bolsillo-jardinero-default"
                        d="m235.65 382.96s22.254 9.2045 39.516-3.8138c0 0 12.119 42.913-22.64 39.035-15.836-1.7668-17.409-16.59-16.877-35.222z"
                        fill={AVATAR_COLORS.overallsShadow}
                        style={{ mixBlendMode: "normal" }}
                    />
                </g>
            );
        }
        return null;
    }

    // IDs unicos para los gradientes basados en el assetKey
    const grad16Id = `grad16-${assetKey}`;
    const grad18Id = `grad18-${assetKey}`;
    const grad20Id = `grad20-${assetKey}`;

    if (config.type === 'remera') {
        if (part === 'right-arm') {
            return (
                <g id={`remera-right-arm-${assetKey}`}>
                    <defs>
                        <radialGradient
                            id={grad18Id}
                            cx="270.42"
                            cy="343.39"
                            r="25.688"
                            gradientTransform="matrix(3.0442 .11656 -.252 6.5817 -471.34 -1947.3)"
                            gradientUnits="userSpaceOnUse"
                        >
                            <stop stopColor={config.base} offset=".2978" />
                            <stop stopColor={config.shadow} offset=".2978" />
                        </radialGradient>
                    </defs>
                    <path
                        id="manga-remera-der"
                        transform="matrix(-.0010412 1 1 .0010412 -191.8 62.96)"
                        d="m293.89 361.82s7.0602-39.393-0.0661-40.812c-2.6418-0.52601-36.579-4.4318-43.127 13.031 0 0-7.8714 15.28 5.0271 26.789 0 0 37.306 4.0262 38.166 0.99218 0-1e-5 -4e-5 0-4e-5 0z"
                        fill={`url(#${grad18Id})`}
                        stroke={config.stroke}
                        strokeWidth="2"
                    />
                </g>
            );
        }

        if (part === 'left-arm') {
            return (
                <g id={`remera-left-arm-${assetKey}`}>
                    <defs>
                        <radialGradient
                            id={grad20Id}
                            cx="189.08"
                            cy="342.62"
                            r="23.328"
                            gradientTransform="matrix(2.8387 1.2232 -1.5269 3.5435 166.79 -1105.8)"
                            gradientUnits="userSpaceOnUse"
                        >
                            <stop stopColor={config.base} offset=".37596" />
                            <stop stopColor={config.shadow} offset=".37596" />
                        </radialGradient>
                    </defs>
                    <path
                        id="manga-remera-izq"
                        transform="translate(-34.507 .33232)"
                        d="m170.99 345.28c-1.8672 10.849 22.954 18.595 35.785 16.073 2.4868-0.4888 9.9727-20.197 8.599-27.054-1.5081-7.5272-7.1526-21.027-26.524-16.801-9.0023 1.9636-14.023 5.4901-17.859 27.781z"
                        fill={`url(#${grad20Id})`}
                        stroke={config.stroke}
                        strokeWidth="2"
                    />
                </g>
            );
        }

        if (part === 'torso') {
            return (
                <g id={`remera-torso-${assetKey}`}>
                    <defs>
                        <radialGradient
                            id={grad16Id}
                            cx="231.82"
                            cy="386.36"
                            r="57.106"
                            gradientTransform="matrix(2.8288 -.59163 1.9979 9.553 -1211.3 -3157.4)"
                            gradientUnits="userSpaceOnUse"
                        >
                            <stop stopColor={config.base} offset=".35537" />
                            <stop stopColor={config.shadow} offset=".35537" />
                        </radialGradient>
                    </defs>
                    <path
                        id="remera-cuerpo"
                        d="m195.89 375.19c-7.1332 34.689-11.913 56.631-7.3552 59.771 11.703 8.0618 79.535 14.433 108.96 1.127 6.0003-2.7138-9.3312-57.098-13.365-71.725-4.6852-15.868-11.103-36.302-14.113-39.477-0.4303-0.45377-3.1037 0.54465-7.1626 1.1104 0.0388 0.55387 0.0584 1.1168 0.0584 1.6879 9e-5 9.9433-8.1294 15.667-12.885 15.223-7.0309-0.65684-15.368-5.8478-16.045-15.036-0.92285-0.0412-27.878-3.0615-28.874-3.101-5.0868 2.3816-5.7725 25.325-9.2139 50.421z"
                        fill={`url(#${grad16Id})`}
                        stroke={config.stroke}
                        strokeWidth="2.0923"
                    />
                </g>
            );
        }
    }

    if (config.type === 'jardinero') {
        if (part === 'torso') {
            return (
                <g id={`jardinero-torso-${assetKey}`}>
                    <path
                        id="jardinero-cuerpo"
                        d="m205.8 325.72s17.212 3.9288 12.348 23.573c0 0-0.74835 29.186-24.322 33.863 0 0-0.37418 0.37418-1.8709 5.7998 0 0 20.206 0.46773 23.199 6.5481 2.9934 6.0804-18.02 37.025-5.0139 44.023 22.054 11.865 50.061 16.881 86.21 3.3106 0 0 10.243-44.597-27.362-116.44 0 0-5.096-2.9262-5.6281 1.3252-0.52059 4.1597 5.8682 17.03 0.91737 22.808-3.7752 4.4056-11.853 3.9891-17.293 1.9733-10.175-3.7701-12.543-18.792-22.13-23.874-5.6771-3.0093-16.048-2.3743-19.055-2.91z"
                        fill={config.base}
                    />
                    <path
                        id="bolsillo-jardinero"
                        d="m235.65 382.96s22.254 9.2045 39.516-3.8138c0 0 12.119 42.913-22.64 39.035-15.836-1.7668-17.409-16.59-16.877-35.222z"
                        fill={config.pocket || config.shadow}
                        style={{ mixBlendMode: "normal" }}
                    />
                </g>
            );
        }
        // Jardineros don't have sleeves
        return null;
    }

    return null;
};
