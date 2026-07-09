import React from "react";
import "./HulyAvatar.css";
import { AvatarBodyClothing } from "./AvatarBodyClothing";
import { AvatarVisor } from "./AvatarVisor";
import { AVATAR_COLORS } from "./AvatarConstants";



export type AvatarAnimation =
  "idle" | "wave" | "blow" | "inhale" | "hold" | "exhale" | "walking";

export interface AvatarEquippedItem {
  assetKey: string;
  category: string;
}

interface HulyAvatarProps {
  equippedItems?: AvatarEquippedItem[];
  animation?: AvatarAnimation;
  pose?: "default" | "sitting";
  view?: "default" | "guided-breathing";
}

const HulyAvatar: React.FC<HulyAvatarProps> = ({
  equippedItems = [],
  animation = "idle",
  pose = "default",
  view = "default",
}) => {
  const equippedAssetKeys = equippedItems.map((item) => item.assetKey);
  const hasShirtEquipped = equippedItems.some(
    (item) => item.category === "SHIRT",
  );
  const activeShirtKey = equippedItems.find((item) => item.category === "SHIRT")?.assetKey;
  const activeHatKey = equippedItems.find((item) => item.category === "HAT")?.assetKey;

  return (
    <svg
      id="svg1"
      width="100%"
      height="100%"
      version="1.1"
      viewBox="0 0 512 512"
      xmlSpace="preserve"
      xmlns="http://www.w3.org/2000/svg"
      xmlnsXlink="http://www.w3.org/1999/xlink"
    >
      <defs id="defs1">
        <linearGradient id="linearGradient97">
          <stop id="stop97" stopColor={AVATAR_COLORS.eyeBase} offset=".43215" />
          <stop id="stop98" stopColor={AVATAR_COLORS.eyeShadow} offset=".43215" />
        </linearGradient>
        <linearGradient id="linearGradient90">
          <stop id="stop90" stopColor={AVATAR_COLORS.white} offset=".3501" />
          <stop id="stop91" stopColor={AVATAR_COLORS.gray} offset=".3501" />
        </linearGradient>
        <linearGradient
          id="linearGradient35"
          x1="237.79"
          x2="231.93"
          y1="389.84"
          y2="260.57"
          gradientTransform="matrix(1 0 0 .74863 0 73.969)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop34" stopColor={AVATAR_COLORS.skinBase} offset="0" />
          <stop id="stop35" stopColor={AVATAR_COLORS.skinShadow1} offset="1" />
        </linearGradient>
        <linearGradient
          id="linearGradient41"
          x1="271.27"
          x2="312.63"
          y1="490.7"
          y2="448.11"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop40" stopColor={AVATAR_COLORS.skinBase} offset="0" />
          <stop id="stop41" stopColor={AVATAR_COLORS.skinShadow2} offset="1" />
        </linearGradient>
        <linearGradient
          id="linearGradient43"
          x1="212.41"
          x2="272.2"
          y1="495.91"
          y2="441.52"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop42" stopColor={AVATAR_COLORS.skinBase} offset="0" />
          <stop id="stop43" stopColor={AVATAR_COLORS.skinShadow3} offset="1" />
        </linearGradient>
        <radialGradient
          id="radialGradient56"
          cx="177.18"
          cy="8.712"
          r="17.725"
          gradientTransform="matrix(-.055449 .67746 -.97904 -.080133 205.63 -109.86)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop55" stopColor={AVATAR_COLORS.shoeBlueBase} offset=".9999" />
          <stop id="stop56" stopColor={AVATAR_COLORS.shoeBlueShadow} offset=".9999" />
        </radialGradient>
        <radialGradient
          id="radialGradient72"
          cx="26.941"
          cy="-30.761"
          r="103.81"
          gradientTransform="matrix(-1.0705 -1.5752 1.8399 -1.2504 297.39 90.655)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop72" stopColor={AVATAR_COLORS.shoeBlueLight} offset=".45113" />
          <stop id="stop75" stopColor={AVATAR_COLORS.mouthDark} offset=".45113" />
          <stop id="stop71" stopColor={AVATAR_COLORS.shoeBlueShadow} offset=".45113" />
        </radialGradient>
        <radialGradient
          id="radialGradient74"
          cx="263.61"
          cy="60.532"
          r="101.84"
          gradientTransform="matrix(1.6385 -.57244 .38093 1.0904 -194.29 140.97)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop73" stopColor={AVATAR_COLORS.shoeBlueLight} offset=".58356" />
          <stop id="stop74" stopColor={AVATAR_COLORS.shoeBlueShadow} offset=".58356" />
        </radialGradient>
        <radialGradient
          id="radialGradient91"
          cx="198.67"
          cy="605.95"
          r="21.438"
          gradientTransform="matrix(-2.6202 1.2036 -1.4425 -3.1403 1622.7 2159.8)"
          gradientUnits="userSpaceOnUse"
          xlinkHref="#linearGradient90"
        />
        <radialGradient
          id="radialGradient91-4"
          cx="198.67"
          cy="605.95"
          r="21.438"
          gradientTransform="matrix(-2.6202 1.2036 -1.4425 -3.1403 1766.4 2242.2)"
          gradientUnits="userSpaceOnUse"
          xlinkHref="#linearGradient90"
        />
        <radialGradient
          id="radialGradient98"
          cx="221.88"
          cy="555.89"
          r="49.707"
          gradientTransform="matrix(2.1829 .026157 -.046862 3.9108 -228.56 -1627.5)"
          gradientUnits="userSpaceOnUse"
          xlinkHref="#linearGradient97"
        />
        <radialGradient
          id="radialGradient113"
          cx="221.88"
          cy="555.89"
          r="49.707"
          gradientTransform="matrix(2.1647 .28218 -.50555 3.8782 88.664 -1664.4)"
          gradientUnits="userSpaceOnUse"
          xlinkHref="#linearGradient97"
        />
        <radialGradient
          id="radialGradient16"
          cx="231.82"
          cy="386.36"
          r="57.106"
          gradientTransform="matrix(2.8288 -.59163 1.9979 9.553 -1211.3 -3157.4)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop15" stopColor={AVATAR_COLORS.shirtVioletBase} offset=".35537" />
          <stop id="stop16" stopColor={AVATAR_COLORS.shirtVioletShadow} offset=".35537" />
        </radialGradient>
        <radialGradient
          id="radialGradient18"
          cx="270.42"
          cy="343.39"
          r="25.688"
          gradientTransform="matrix(3.0442 .11656 -.252 6.5817 -471.34 -1947.3)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop17" stopColor={AVATAR_COLORS.shirtVioletBase} offset=".2978" />
          <stop id="stop18" stopColor={AVATAR_COLORS.shirtVioletShadow} offset=".2978" />
        </radialGradient>
        <radialGradient
          id="radialGradient20"
          cx="189.08"
          cy="342.62"
          r="23.328"
          gradientTransform="matrix(2.8387 1.2232 -1.5269 3.5435 166.79 -1105.8)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop19" stopColor={AVATAR_COLORS.shirtVioletBase} offset=".37596" />
          <stop id="stop20" stopColor={AVATAR_COLORS.shirtVioletShadow} offset=".37596" />
        </radialGradient>
        <clipPath id="clipPath26">
          <ellipse
            id="ellipse27"
            cx="218.7"
            cy="257.14"
            rx="6.9618"
            ry="21.09"
            fill={AVATAR_COLORS.pantsGreenBase}
          />
        </clipPath>
        <radialGradient
          id="radialGradient82"
          cx="180.98"
          cy="311.84"
          r="10.131"
          gradientTransform="matrix(2.9858 0 0 1.392 -222.82 -129.2)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop80" stopColor={AVATAR_COLORS.blush} offset=".050035" />
          <stop id="stop81" stopColor={AVATAR_COLORS.skinBase2} stopOpacity="0" offset="1" />
        </radialGradient>
        <radialGradient
          id="radialGradient84"
          cx="180.98"
          cy="311.84"
          r="48.554"
          gradientTransform="matrix(1 0 0 .46622 0 166.45)"
          gradientUnits="userSpaceOnUse"
        >
          <stop id="stop83" stopColor={AVATAR_COLORS.blush} offset="0" />
          <stop id="stop84" stopColor={AVATAR_COLORS.skinBase2} offset="1" />
        </radialGradient>
      </defs>
      <g className={animation === "wave" ? "anim-wave-hand" : (pose === "sitting" ? "pose-sit-arm-der" : (animation === "walking" ? "anim-walk-arm-der" : ""))}>
        <g
          id="g101-5-2"
          transform="matrix(-.0010412 1 1 .0010412 -63.159 191.73)"
        >
          <g fill={AVATAR_COLORS.skinBase}>
            <ellipse
              id="circulo-union-brazo-inferior-der"
              cx="148.27"
              cy="370.86"
              rx="15.456"
              ry="16.233"
              strokeWidth=".13372"
            />
            <path
              id="brazo-inferior-der"
              d="m161.32 379.56s-5.9827 10.954-16.04 21.665c0 0-18.506-13.511-18.506-13.51 0 0 5.5265-13.231 6.1578-18.325"
              strokeWidth=".15875"
            />
            <ellipse
              id="circulo-union-muneca-der"
              transform="matrix(-.0010412 1 1 .0010412 -191.8 62.96)"
              cx="329.95"
              cy="329.05"
              rx="11.553"
              ry="11.553"
            />
            <ellipse
              id="circulo-union-mano-der"
              transform="matrix(-.0010412 1 1 .0010412 -191.8 62.96)"
              cx="329.95"
              cy="329.05"
              rx="11.553"
              ry="11.553"
            />
            <path
              id="mano-der"
              transform="matrix(-.0010412 1 1 .0010412 -191.8 62.96)"
              d="m337.89 337.41s10.6-8.6651 12.154-19.745c0 0 1.7859-9.8888-6.9453-9.955 0 0-5.4034 0.70525-18.64 11.229"
            />
          </g>
          <g id="g64">
            <ellipse
              id="circulo-union-brazo-superior-der"
              cx="148.24"
              cy="370.83"
              rx="15.456"
              ry="16.233"
              fill={AVATAR_COLORS.skinBase}
              strokeWidth=".13372"
            />
            <path
              id="brazo-superior-der"
              d="m132.85 369.67s-4.0712-32.918 1.2608-42.6c0 0 3.7232-12.083 16.857-12.94 0 0 14.407-0.56372 18.195 11.316 0 0 2.5476 7.9103 0.79145 20.721-1.4487 10.568-2.8014 18.428-7.5253 31.103"
              fill={AVATAR_COLORS.skinBase}
              strokeWidth=".15875"
            />
            {hasShirtEquipped && <AvatarBodyClothing assetKey={activeShirtKey} part="right-arm" />}
          </g>
        </g>
      </g>
      <g id="pierna-der-completa" className={pose === "sitting" ? "pose-sit-leg-der" : (animation === "walking" ? "anim-walk-leg-der" : "")}>
        <g id="piel-muslo-der" fill={AVATAR_COLORS.skinBase}>
          <ellipse
            id="circulo-union-pierna-superior-der"
            cx="272.16"
            cy="451.28"
            rx="17.693"
            ry="17.765"
          />
          {/* <ellipse
            id="circulo-union-pierna-inferior-der"
            cx="289.66"
            cy="453.34"
            rx="17.693"
            ry="17.765"
          /> */}
          <path
            id="pierna-superior-der"
            d="m254.53 449.9 0.60804-20.206s19.165-18.732 38.545 1.8395l-3.9688 21.894"
          />
        </g>



        <g className={pose === "sitting" ? "pose-sit-calf-der" : ""}>
          <ellipse
            id="circulo-union-pierna-inferior-der"
            cx="272.16"
            cy="451.28"
            rx="17.693"
            ry="17.765"
            fill={AVATAR_COLORS.skinBase}
          />
          <g id="piel-pantorrilla-der" fill={AVATAR_COLORS.skinBase}>
            <path
              id="pierna-inferior-der"
              d="m289.66 453.34s-3.447 22.58-10.26 42.292c0 0-1.7604 7.9184-14.015 1.9784 0 0-13.47-7.2965-12.114-17.212l1.2628-30.542"
            />
          </g>

          <g id="pie-der">
            <g id="piel-pie-der-folder">
              <path
                id="piel-pie-der"
                d="m253.97 484.49s-1.4032 13.497 7.684 18.575c0 0 14.165 9.3544 23.319 1.9377 0 0 9.6217-9.8222-2.4054-19.444 0 0-13.297-13.898-28.598-1.0691z"
                fill="url(#linearGradient41)"
                strokeWidth=".15875"
              />
              {!equippedAssetKeys.includes("zapatillas-blancas") && (
                <ellipse
                  id="circulo-union-tobillo-der"
                  cx="268.14"
                  cy="486.16"
                  rx="14.403"
                  ry="14.403"
                  fill={AVATAR_COLORS.skinBase}
                />
              )}
            </g>
            {equippedAssetKeys.includes("zapatillas-blancas") && (
              <g
                id="zapatilla-blanca-pie-der"
                transform="translate(-90.419 -84.431)"
                stroke={AVATAR_COLORS.shoeGrayStroke}
              >
                <path
                  id="path79-7-7"
                  d="m379.43 569.32s-8.5045-3.4018-15.308 2.5514m17.73 0.38629s-8.5045-3.4018-15.308 2.5514m19.56 10.867s-23.057 10.583-41.577-9.7329m38.932-0.47247s-14.93-5.9531-18.993 11.434m-3.0238-16.631 7.5595 7.3706m-24.663-11.245s17.292 6.4256 18.048 2.6458c0 0 4.8192-10.016 13.891-1.9844 0 0 12.568 10.205 9.0714 18.993 0 0-0.47247 6.4256-11.434 7.087 0 0-11.434 1.5119-22.868-5.1027 0 0-8.5045-1.6064-6.7091-21.639z"
                  fill="url(#radialGradient91-4)"
                />
                <g fill={AVATAR_COLORS.white} strokeWidth=".35714">
                  <path
                    id="path91-8"
                    d="m369.09 587.79c-0.15592-0.0212-0.75123-0.0849-1.3229-0.14166-5.6614-0.56173-11.518-2.82-16.37-6.3122-1.8235-1.3124-1.6493-1.0897-1.8439-2.357-0.26395-1.7186-0.32472-5.1007-0.12378-6.8895 0.14241-1.2678 0.45727-3.1066 0.54737-3.1967 0.0177-0.0177 0.79608 0.17923 1.7297 0.43762 3.5764 0.98975 6.1732 1.4356 8.1396 1.3973l1.2448-0.0241 3.6284 3.5389 3.6284 3.5389-0.46049 0.51972c-1.0676 1.2049-2.2466 3.2586-2.9471 5.1336-0.43514 1.1647-1.0198 3.1827-0.96411 3.3278 0.0231 0.0601 0.25833 0.15206 0.52282 0.20434l0.4809 0.0951 0.0943-0.38801c0.0519-0.21341 0.30787-1.0471 0.56883-1.8527 1.7832-5.5047 5.1138-8.7579 9.8256-9.5976 1.1918-0.21238 3.4999-0.2136 4.7719-3e-3 1.2391 0.20566 2.7611 0.61651 2.9801 0.80449 0.19613 0.16831 0.9727 1.702 1.364 2.6938 0.14201 0.35996 0.36737 1.0616 0.50079 1.5592 0.21394 0.7978 0.24265 1.0888 0.24313 2.4638 5.3e-4 1.2675-0.0336 1.674-0.18214 2.1734l-0.18269 0.6142-0.89693 0.31366c-2.1206 0.74158-4.8176 1.3641-7.4643 1.723-1.1499 0.15595-2.0839 0.20543-4.3467 0.23032-1.5851 0.0175-3.0096 0.0143-3.1656-7e-3z"
                  />
                  <path
                    id="path92-6"
                    d="m368.12 576-0.97471-0.97803 0.36218-0.28223c1.6543-1.289 3.577-2.166 5.7979-2.6446 0.64587-0.13915 1.2126-0.1718 2.9271-0.1686 1.9287 4e-3 2.2344 0.0271 3.2918 0.25258 0.68521 0.1461 1.242 0.31763 1.3508 0.41616 0.24286 0.21979 1.4398 1.8285 1.3935 1.8729-0.0197 0.0188-0.39371-0.0437-0.83117-0.13903-2.7642-0.60249-6.0538-0.45286-8.3334 0.37906-1.0413 0.38003-2.3898 1.1057-3.2307 1.7386-0.38817 0.29213-0.72216 0.53118-0.7422 0.53118-0.02 0-0.47505-0.44013-1.0111-0.97806z"
                  />
                  <path
                    id="path93-8"
                    d="m365.27 573.25c-0.54385-0.5457-0.94025-0.99219-0.88091-0.99219 0.0594 0 0.4963-0.29165 0.971-0.64812 2.6296-1.9747 5.9999-2.9169 9.4888-2.6528 1.138 0.0861 2.1718 0.24614 2.9577 0.45775 0.28284 0.0762 0.53231 0.27837 1.0575 0.85723 0.37806 0.41666 0.6526 0.75723 0.61009 0.75681-0.0425 0-0.58594-0.0642-1.2076-0.14174-4.1271-0.51477-8.1301 0.48565-11.2 2.7992-0.40589 0.30583-0.75358 0.55607-0.77264 0.55607-0.019 0-0.47963-0.44648-1.0235-0.99219z"
                  />
                </g>
              </g>
            )}
          </g>
        </g>
      </g>
      <g id="g110" className={pose === "sitting" ? "pose-sit-leg-izq" : (animation === "walking" ? "anim-walk-leg-izq" : "")}>
        <g
          id="g56"
          transform="matrix(.98268 -.20431 .18529 1.0836 -138.2 10.849)"
        >
          <g fill={AVATAR_COLORS.skinBase}>
            <ellipse
              id="circulo-union-pierna-superior-izq"
              cx="272.16"
              cy="451.28"
              rx="17.563"
              ry="17.765"
            />
            <path
              id="pierna-superior-izq"
              d="m254.53 449.9 0.60804-20.206s19.165-18.732 38.545 1.8395l-3.9688 21.894"
            />
          </g>
        </g>




        <g className={pose === "sitting" ? "pose-sit-calf-izq" : ""}>
          <g transform="matrix(.98268 -.20431 .18529 1.0836 -138.2 10.849)" fill={AVATAR_COLORS.skinBase}>
            <ellipse
              id="circulo-union-pierna-inferior-izq"
              cx="272.16"
              cy="451.28"
              rx="17.563"
              ry="17.765"
            />
            <path
              id="pierna-inferior-izq"
              d="m289.66 453.34s-3.447 22.58-10.26 42.292c0 0-1.7604 7.9184-14.015 1.9784 0 0-13.47-7.2965-12.114-17.212l1.2628-30.542"
            />
          </g>

          <g id="pie-izq">
            <g id="piel-pie-izq-folder">
              <path
                id="piel-pie-izq"
                d="m201.39 485.76s3.4745 21.382 16.036 22.785c0 0 11.96 3.4077 18.375-5.7463 0 0 5.479-8.6863-5.3454-16.638 0 0-15.769-16.972-29.066-0.40091z"
                fill="url(#linearGradient43)"
                strokeWidth=".15875"
              />
              {!equippedAssetKeys.includes("zapatillas-blancas") && (
                <ellipse
                  id="circulo-union-tobillo-izq"
                  cx="215.12"
                  cy="484.6"
                  rx="13.781"
                  ry="13.781"
                  fill={AVATAR_COLORS.skinBase3}
                />
              )}
            </g>
            {equippedAssetKeys.includes("zapatillas-blancas") && (
              <g id="zapatilla-blanca-pie-izq" stroke={AVATAR_COLORS.shoeGrayStroke}>
                <path
                  id="path79-7"
                  d="m235.8 486.92s-8.5045-3.4018-15.308 2.5514m17.73 0.38629s-8.5045-3.4018-15.308 2.5514m19.56 10.867s-23.057 10.583-41.577-9.7329m38.932-0.47247s-14.93-5.9531-18.993 11.434m-3.0238-16.631 7.5595 7.3706m-24.663-11.245s17.292 6.4256 18.048 2.6458c0 0 4.8192-10.016 13.891-1.9844 0 0 12.568 10.205 9.0714 18.993 0 0-0.47247 6.4256-11.434 7.087 0 0-11.434 1.5119-22.868-5.1027 0 0-8.5045-1.6064-6.7091-21.639z"
                  fill="url(#radialGradient91)"
                />
                <g fill={AVATAR_COLORS.white} strokeWidth=".35714">
                  <path
                    id="path91"
                    d="m225.46 505.39c-0.15592-0.0212-0.75123-0.0849-1.3229-0.14166-5.6614-0.56173-11.518-2.82-16.37-6.3122-1.8235-1.3124-1.6493-1.0897-1.8439-2.357-0.26395-1.7186-0.32472-5.1007-0.12378-6.8895 0.14241-1.2678 0.45727-3.1066 0.54737-3.1967 0.0177-0.0177 0.79608 0.17923 1.7297 0.43762 3.5764 0.98975 6.1732 1.4356 8.1396 1.3973l1.2448-0.0241 3.6284 3.5389 3.6284 3.5389-0.46049 0.51972c-1.0676 1.2049-2.2466 3.2586-2.9471 5.1336-0.43514 1.1647-1.0198 3.1827-0.96411 3.3278 0.0231 0.0601 0.25833 0.15206 0.52282 0.20434l0.4809 0.0951 0.0943-0.38801c0.0519-0.21341 0.30787-1.0471 0.56883-1.8527 1.7832-5.5047 5.1138-8.7579 9.8256-9.5976 1.1918-0.21238 3.4999-0.2136 4.7719-3e-3 1.2391 0.20566 2.7611 0.61651 2.9801 0.80449 0.19613 0.16831 0.9727 1.702 1.364 2.6938 0.14201 0.35996 0.36737 1.0616 0.50079 1.5592 0.21394 0.7978 0.24265 1.0888 0.24313 2.4638 5.3e-4 1.2675-0.0336 1.674-0.18214 2.1734l-0.18269 0.6142-0.89693 0.31366c-2.1206 0.74158-4.8176 1.3641-7.4643 1.723-1.1499 0.15595-2.0839 0.20543-4.3467 0.23032-1.5851 0.0175-3.0096 0.0143-3.1656-7e-3z"
                  />
                  <path
                    id="path92"
                    d="m224.48 493.6-0.97471-0.97803 0.36218-0.28223c1.6543-1.289 3.577-2.166 5.7979-2.6446 0.64587-0.13915 1.2126-0.1718 2.9271-0.1686 1.9287 4e-3 2.2344 0.0271 3.2918 0.25258 0.68521 0.1461 1.242 0.31763 1.3508 0.41616 0.24286 0.21979 1.4398 1.8285 1.3935 1.8729-0.0197 0.0188-0.39371-0.0437-0.83117-0.13903-2.7642-0.60249-6.0538-0.45286-8.3334 0.37906-1.0413 0.38003-2.3898 1.1057-3.2307 1.7386-0.38817 0.29213-0.72216 0.53118-0.7422 0.53118-0.02 0-0.47505-0.44013-1.0111-0.97806z"
                  />
                  <path
                    id="path93"
                    d="m221.64 490.85c-0.54385-0.5457-0.94025-0.99219-0.88091-0.99219 0.0594 0 0.4963-0.29165 0.971-0.64812 2.6296-1.9747 5.9999-2.9169 9.4888-2.6528 1.138 0.0861 2.1718 0.24614 2.9577 0.45775 0.28284 0.0762 0.53231 0.27837 1.0575 0.85723 0.37806 0.41666 0.6526 0.75723 0.61009 0.75681-0.0425 0-0.58594-0.0642-1.2076-0.14174-4.1271-0.51477-8.1301 0.48565-11.2 2.7992-0.40589 0.30583-0.75358 0.55607-0.77264 0.55607-0.019 0-0.47963-0.44648-1.0235-0.99219z"
                  />
                </g>
              </g>
            )}
          </g>
        </g>
      </g>
      <g id="g99" className={pose === "sitting" ? "pose-sit-torso" : ""}>
        <g id="piel-torso" transform="translate(2.3221e-6)">
          <path
            id="piel-torso-completo"
            d="m204.14 324.05s-9.7727 21.699-11.256 83.936c0 0 59.323 10.659 102.59 10.646 0 0-4.4727-53.2-26.95-94.247l-30.174 3.7637z"
            fill="url(#linearGradient35)"
            strokeWidth=".13736"
          />
        </g>
        <path
          id="piel-cintura"
          d="m192.92 407.78s69.022 6.414 102.14 7.2559c0 0 1.2114 9.808-1.3625 17.1 0 0-30.516-5.089-37.849 8.0808 0 0-16.948 2.3453-24.618-2.0513 0 0-6.3025-10.209-39.511-13.484 0 0 0.29546-8.8993 1.2033-16.902z"
          fill={AVATAR_COLORS.skinBase}
        />

        <AvatarBodyClothing assetKey={activeShirtKey} part="torso" />
      </g>
      <g className={animation === "walking" ? "anim-walk-arm-izq-wrap" : ""}>
        <g
          id="g101-5"
          className={view === "guided-breathing" ? "pose-guided-breathing-arm-izq" : ""}
          transform={view === "guided-breathing" ? undefined : "translate(34.507 -.33232)"}
        >
          <g fill={AVATAR_COLORS.skinBase}>
            <ellipse
              id="circulo-union-brazo-inferior-izq"
              cx="148.27"
              cy="370.86"
              rx="15.456"
              ry="16.233"
              strokeWidth=".13372"
            />
            <path
              id="brazo-inferior-izq"
              d="m161.32 379.56s-5.6597 11.046-15.717 21.756c0.0577-0.0866-20.34-8.8498-20.701-9.1029 0 0 0.21854-2.6558 3.4809-8.3931 0 0 4.0178-10.56 4.6491-15.654"
              strokeWidth=".15875"
            />
            <ellipse
              id="circulo-union-muneca-izq"
              cx="135.03"
              cy="398.92"
              rx="11.553"
              ry="11.553"
            />
            <ellipse
              id="circulo-union-mano-izq"
              cx="135.03"
              cy="398.92"
              rx="11.553"
              ry="11.553"
            />
            <path
              id="mano-izq"
              d="m142.97 407.28s10.6-8.6651 12.154-19.745c0 0 1.7859-9.8888-6.9453-9.955 0 0-5.4034 0.70525-18.64 11.229"
            />
          </g>
          <g id="g88">
            <ellipse
              id="circulo-union-brazo-superior-izq"
              cx="148.24"
              cy="370.83"
              rx="15.456"
              ry="16.233"
              fill={AVATAR_COLORS.skinBase}
              strokeWidth=".13372"
            />
            <path
              id="brazo-superior-izq"
              d="m132.85 369.67s-4.0712-32.918 1.2608-42.6c0 0 3.7232-12.083 16.857-12.94 0 0 14.407-0.56372 18.195 11.316 0 0 2.5476 7.9103 0.79145 20.721-1.4487 10.568-2.8014 18.428-7.5253 31.103"
              fill={AVATAR_COLORS.skinBase}
              strokeWidth=".15875"
              transform="rotate(15, 150, 370)"
            />
            {hasShirtEquipped && <AvatarBodyClothing assetKey={activeShirtKey} part="left-arm" />}
          </g>
        </g>
      </g>
      <g
        id="g102"
        className={pose === "sitting" ? "pose-sit-head" : (animation === "wave" ? "anim-head-bob" : "idle-head-bob")}
      >
        <g id="piel-cara">
          <path
            id="path3"
            transform="scale(.26458)"
            d="m873.57 1240.5c-78.911-2.8491-153.03-19.492-218.57-49.078-8.25-3.7242-22.518-10.123-31.707-14.22-78.598-35.041-151.33-95.911-201.47-168.6-71.086-103.07-100.91-229.17-83.122-351.43 15.554-106.91 64.038-203.5 140.56-280.02 80.346-80.352 183.15-129.42 297.88-142.17 23.763-2.6417 74.251-2.9699 97.857-0.63611 115.55 11.423 220.48 60.764 302.24 142.12 113.87 113.31 165.4 271.91 139.7 430-3.0828 18.965-2.0176 58.533 2.2252 82.651 4.1386 23.526 9.6547 38.906 23.565 65.703 29.554 56.934 33.233 83.08 16.579 117.81-20.209 42.144-50.928 66.126-124.31 97.047-82.519 34.771-168.57 57.123-257 66.752-20.266 2.207-76.851 5.2508-88 4.7338-3.1429-0.1458-10.536-0.4391-16.429-0.6519z"
            fill={AVATAR_COLORS.skinBase2}
            stroke={AVATAR_COLORS.skinShadow4}
            strokeWidth="7.5591"
          />
        </g>
        <g id="mejilla-izq">
          {(animation === "blow" || animation === "exhale") && (
            <ellipse
              id="mejilla-izq-soplo"
              transform="matrix(.99505 -.099423 .079789 .99681 0 0)"
              cx="180.98"
              cy="311.84"
              rx="48.554"
              ry="22.637"
              fill="url(#radialGradient84)"
              strokeWidth=".77603"
            />
          )}
          {animation !== "blow" &&
            animation !== "exhale" &&
            animation !== "inhale" && (
              <ellipse
                id="path11"
                transform="matrix(.99505 -.099423 .079789 .99681 0 0)"
                cx="180.98"
                cy="311.84"
                rx="10.131"
                ry="4.7233"
                fill={AVATAR_COLORS.blush}
                strokeWidth=".16192"
              />
            )}
        </g>
        <g id="mejilla-der">
          {(animation === "blow" || animation === "exhale") && (
            <ellipse
              id="mejilla-der-soplo"
              transform="matrix(.99505 -.099423 .079789 .99681 0 0)"
              cx="317.53"
              cy="304.88"
              rx="30.249"
              ry="14.103"
              fill="url(#radialGradient82)"
              strokeWidth=".48346"
            />
          )}
          {animation !== "blow" &&
            animation !== "exhale" &&
            animation !== "inhale" && (
              <ellipse
                id="path11-9"
                transform="matrix(.99505 -.099423 .079789 .99681 0 0)"
                cx="322.16"
                cy="306.07"
                rx="10.131"
                ry="4.7233"
                fill={AVATAR_COLORS.blush}
                strokeWidth=".16192"
              />
            )}
        </g>
        <g
          id="ceja-der"
          className={`organic-transform ${animation === "wave" ? "anim-ceja-der" : "idle-ceja-der"}`}
        >
          <path
            id="path10-3"
            d="m309.27 204.19s9.8044-6.7331 22.769 0.97448c0 0 1.3091 2.3578-1.6292 2.4781 0 0-9.4306-6.0685-19.413-0.66826 0 0-2.4466-0.48784-1.7277-2.7843z"
            fill={AVATAR_COLORS.pantsGreenShadow1}
            strokeWidth=".15875"
          />
        </g>
        <g
          id="ceja-izq"
          className={`organic-transform ${animation === "wave" ? "anim-ceja-izq" : "idle-ceja-izq"}`}
        >
          <path
            id="path10"
            d="m194.04 222.23s7.0826-9.5549 21.849-6.4813c0 0 2.0045 1.8041-0.73499 2.8731 0 0-10.891-2.6727-18.575 5.6795 0 0-2.4722 0.33409-2.5391-2.0713z"
            fill={AVATAR_COLORS.pantsGreenShadow1}
            strokeWidth=".15875"
          />
        </g>
        <g id="ojos">
          <g id="g30">
            <g
              id="ojo-cerrado-feliz-der"
              className={`organic-transform ${animation === "wave" ? "anim-happy-eyes" : "idle-happy-eyes"}`}
            >
              <path
                id="ojo-cerrado-alegre-der"
                d="m311.68 264.81s0-20.127 9.2604-20.6c0 0 10.489-3.7798 14.647 17.103 0 0 2.0789 3.1183 3.3073 0.56698 0 0 2.7403-15.686-10.394-24.19 0 0-10.583-5.1972-18.143 5.1972 0 0-5.6696 10.678-2.1734 21.45 0 0 1.6064 4.0632 3.4963 0.47246z"
                fill={AVATAR_COLORS.pantsGreenBase}
                strokeWidth=".15875"
              />
            </g>
            <g
              id="ojo-cerrado-feliz-izq"
              className={`organic-transform ${animation === "wave" ? "anim-happy-eyes" : "idle-happy-eyes"}`}
              fill={AVATAR_COLORS.pantsGreenBase}
            >
              <path
                id="ojo-cerrado-alegre-izq"
                d="m207.13 278.47s0-20.127 9.2604-20.6c0 0 10.489-3.7798 14.647 17.103 0 0 2.0789 3.1183 3.3073 0.56698 0 0 2.7403-15.686-10.394-24.19 0 0-10.583-5.1972-18.143 5.1972 0 0-5.6696 10.678-2.1734 21.45 0 0 1.6064 4.0632 3.4963 0.47246z"
                fill={AVATAR_COLORS.pantsGreenBase}
                strokeWidth=".15875"
              />
            </g>
          </g>
          <g
            id="g27"
            className={
              animation === "wave" ? "anim-normal-eyes" : "idle-normal-eyes"
            }
          >
            <g id="g29" transform="translate(103.27 -10.477)">
              <ellipse
                id="ellipse28"
                cx="219.3"
                cy="257.14"
                rx="6.3667"
                ry="21.09"
                fill={AVATAR_COLORS.pantsGreenBase}
              />
              <ellipse
                id="ellipse29"
                transform="matrix(.84083 0 0 1 35.63 0)"
                cx="220.94"
                cy="260.01"
                rx="2.909"
                ry="8.8125"
                clipPath="url(#clipPath26)"
                fill={AVATAR_COLORS.pantsGreenShadow2}
              />
            </g>
            <g id="g28">
              <ellipse
                id="ojo"
                cx="218.7"
                cy="257.14"
                rx="6.962"
                ry="21.09"
                fill={AVATAR_COLORS.pantsGreenBase}
              />
              <ellipse
                id="ojo-pupila"
                cx="220.94"
                cy="260.01"
                rx="2.909"
                ry="8.8125"
                clipPath="url(#clipPath26)"
                fill={AVATAR_COLORS.pantsGreenShadow2}
              />
            </g>
          </g>
          {/* Extra Ojos Cerrados removidos temporalmente para evitar overlaps */}
        </g>
        <g id="hoja-verde-superior">
          <path
            id="hoja-verde-cabeza"
            d="m171.31 6.998c-0.85077 0.030292-1.1276 2.357-1.1276 2.357-0.94494 25.891 8.8821 35.813 8.8821 35.813 9.5439 11.245 18.623 13.071 21.64 13.04 3.1179-0.0318 6.099 1.0366 7.9437 1.8707 3.5123 7.2924 5.8779 16.015 5.5671 26.115 0 0 4.5435 1.6037 7.6171-0.26717 0 0-0.90365-14.565-9.7317-29.593 1.8276-9.7695-4.2995-19.384-4.2995-19.384-5.3454-10.023-22.499-17.39-22.499-17.39-12.379-4.9137-13.04-11.812-13.04-11.812-0.3765-0.55368-0.69092-0.75961-0.95136-0.75034z"
            fill={AVATAR_COLORS.pantsGreenShadow3}
            strokeWidth=".15875"
          />
        </g>
        <g id="g31" fill={AVATAR_COLORS.pantsGreenBase}>
          <g
            id="boca-feliz-abierta"
            className={`organic-transform ${animation === "wave" ? "anim-pop-in-mouth" : "idle-pop-in-mouth"}`}
            fill={AVATAR_COLORS.tongueLight}
          >
            <path
              id="path17"
              d="m257.07 287.36 40.821-5.2444s3.638 14.505-11.009 22.962c0 0-12.237 5.8114-24.001-2.7403 0 0-6.4728-6.6618-5.8114-14.977z"
              fill={AVATAR_COLORS.tongueLight}
              strokeWidth=".15875"
            />
          </g>
          <g
            id="g36"
            className={`organic-transform ${animation === "wave"
              ? "anim-fade-out-mouth"
              : animation === "blow" ||
                animation === "exhale" ||
                animation === "inhale"
                ? "idle-pop-in-mouth"
                : "idle-fade-out-mouth"
              }`}
          >
            <path
              id="boca-sonriente"
              d="m265.52 293.98s2.484 4.4087 9.9488 7.278c2.7252 1.0475 12.175 0.32028 14.876-1.348 3.0417-1.8792 7.0689-5.682 7.7559-12.166"
              fill="none"
              stroke={AVATAR_COLORS.pantsGreenBase}
              strokeLinecap="square"
              strokeLinejoin="bevel"
              strokeWidth="4"
            />
          </g>
          <g
            id="g61"
            className={`organic-transform ${animation === "blow" ||
              animation === "exhale" ||
              animation === "inhale"
              ? "idle-fade-out-mouth"
              : "idle-pop-in-mouth"
              }`}
          >
            <ellipse
              id="boca-soplando"
              cx="276.93"
              cy="302.63"
              rx="7.203"
              ry="9.1673"
              fill={AVATAR_COLORS.tongue}
              fillOpacity=".60392"
            />
            <path
              id="mejilla-cachete"
              d="m262.33 313.6s3.3302-0.8432 6.538-5.2235c1.1711-1.5992 2.3756-7.92 1.7629-9.9859-0.69012-2.3271-2.4652-5.642-6.5914-7.2455"
              fill="none"
              stroke={AVATAR_COLORS.skinShadow4}
              strokeLinecap="square"
              strokeLinejoin="bevel"
              strokeWidth="2.7155"
            />
            <g
              id="g74"
              fill="none"
              stroke={AVATAR_COLORS.shoeLaceStroke}
              strokeLinecap="round"
              className={
                animation === "blow" || animation === "exhale"
                  ? "anim-viento-soplado"
                  : "hidden-wind"
              }
            >
              <g strokeLinejoin="bevel">
                <path
                  id="viento-10"
                  d="m288.12 299.72s113.52-1.266 103.47-34.604c0 0-6.929-14.601-20.025 1.863"
                  strokeWidth="3"
                />
                <path
                  id="viento-9"
                  d="m288.12 301.96s60.617 8.0448 43.966 28.25"
                  strokeWidth="2"
                />
                <path
                  id="viento-8"
                  d="m289.85 300.51s61.834 1.4411 89.563-6.9487"
                  strokeWidth="2.1119"
                />
                <path
                  id="viento-7"
                  d="m288.74 302.1s90.564-6.2442 101.74 26.061c4.877 14.103-5.4146 20.142-13.728 20.97-7.3463 0.73112-17.812-4.3058-15.545-15.776"
                  strokeWidth="2.6453"
                />
              </g>
              <g strokeWidth="1.5">
                <path
                  id="viento-6"
                  d="m366.58 302.02s25.929 4.101 28.707 16.536"
                />
                <path
                  id="viento-5"
                  d="m357.06 312.74c11.642 3.4396 15.346 9.6573 15.346 9.6573"
                />
                <path
                  id="viento-4"
                  d="m365.26 352.29s12.435 7.4083 20.373 0"
                />
                <path
                  id="viento-3"
                  d="m372.93 281.25s9.2604-2.249 10.716-8.599"
                />
                <path
                  id="path73"
                  d="m379.28 254.66s8.3344-3.9688 13.229 3.7042"
                />
                <path
                  id="path74"
                  d="m334.14 309.82s10.29 3.1805 6.7352 13.47"
                />
              </g>
            </g>
          </g>
        </g>
        <g id="pelo-verde">
          <path
            id="path5"
            d="m224.22 61.408s48.681-6.0342 98.574 40.268c0 0 20.789 14.552 35.53 58.208 0 0 6.9926 14.741 14.174 25.513 0 0 15.119 15.875 3.2128 17.576 0 0-5.6696 5.1027-25.513-12.095l-0.25984 9.5203s3.4726-29.931-52.09-61.114c0 0-6.8036 38.176-37.42 60.098 0 0-5.1027 1.7009-6.2366-4.7247 0 0-4.1577-32.884-39.876-58.775 0 0-4.3467-7.1816-8.3155 7.1816 0 0-3.2128 20.6-25.135 33.64 0 0-22.49 12.851-30.427 18.899 0 0-60.382 25.135-22.773 90.525 0 0-20.883-20.033-34.112-52.917 0 0-5.6696 6.9926-19.466 14.174 0 0-11.717 2.6458-5.1027-12.284s17.576-21.356 15.308-48.381c0 0 2.0789-56.885 46.491-91.848 0 0 43.387-38.166 93.438-33.464z"
            fill={AVATAR_COLORS.pantsGreenLight}
            stroke={AVATAR_COLORS.pantsGreenStroke}
            strokeWidth="2"
          />
        </g>
        <AvatarVisor assetKey={activeHatKey} />
      </g>
    </svg>
  );
};

export default HulyAvatar;
