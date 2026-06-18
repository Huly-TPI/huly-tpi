const SECOND_LEVEL_TLDS = new Set(['ac', 'co', 'com', 'edu', 'gov', 'net', 'org']);

export const normalizeDomain = (value: string): string => {
  const domain = value.trim().toLowerCase().replace(/:\d+$/, '').replace(/\.$/, '');

  if (!domain || domain === 'localhost' || /^\d{1,3}(\.\d{1,3}){3}$/.test(domain)) {
    return domain;
  }

  const labels = domain.split('.').filter(Boolean);
  if (labels.length <= 2) {
    return domain.replace(/^www\./, '');
  }

  const last = labels[labels.length - 1];
  const secondLast = labels[labels.length - 2];
  const thirdLast = labels[labels.length - 3];

  if (last.length === 2 && SECOND_LEVEL_TLDS.has(secondLast) && thirdLast) {
    return `${thirdLast}.${secondLast}.${last}`;
  }

  return `${secondLast}.${last}`;
};
