export const CPF_DIGITS = 11;

export function onlyDigits(value: string): string {
  return value.replace(/\D/g, "");
}

export function maskCpf(value: string): string {
  return onlyDigits(value)
    .slice(0, CPF_DIGITS)
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

export function isCpfShapeValid(value: string): boolean {
  return onlyDigits(value).length === CPF_DIGITS;
}

export const passwordRules = {
  length: (value: string) => value.length >= 8,
  upper: (value: string) => /[A-Z]/.test(value),
  lower: (value: string) => /[a-z]/.test(value),
  number: (value: string) => /\d/.test(value),
  symbol: (value: string) => /[^A-Za-z0-9]/.test(value),
};

export function isStrongPassword(value: string): boolean {
  return Object.values(passwordRules).every((rule) => rule(value));
}

export function isValidEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
}
