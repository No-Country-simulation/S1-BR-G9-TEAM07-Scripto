import { apiRequest } from "./api";

export type UserViewDTO = {
  id: number;
  fullName: string;
  email: string;
  cpf?: string;
};

export type UserProfileUpdateDTO = {
  fullName?: string;
  email?: string;
};

export type UserPasswordChangeDTO = {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
};

export async function getCurrentUser(): Promise<UserViewDTO> {
  return apiRequest<UserViewDTO>({ method: "GET", url: "/user/me" });
}

export async function updateOwnProfile(request: UserProfileUpdateDTO): Promise<UserViewDTO> {
  return apiRequest<UserViewDTO>({ method: "PATCH", url: "/user/me/profile", data: request });
}

export async function changeOwnPassword(request: UserPasswordChangeDTO): Promise<void> {
  return apiRequest<void>({ method: "PATCH", url: "/user/me/password", data: request });
}

export type UserProfileStatsDTO = {
  totalProcessedDocuments: number;
  mostFrequentCategory: string | null;
  mostFrequentLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | null;
};

export async function getOwnStatistics(): Promise<UserProfileStatsDTO> {
  return apiRequest<UserProfileStatsDTO>({ method: "GET", url: "/user/me/statistics" });
}
