import { ApiError, apiRequest } from "./api";
import { clearAuthSession, currentSession, saveAccessToken, saveAuthSession, type AuthSession } from "./session";
import { getCurrentUser, type UserViewDTO } from "./user.service";

export type LoginDTO = {
  email: string;
  password: string;
};

export type TokenJWTDTO = {
  token: string;
};

export type UserRegisterDTO = {
  cpf: string;
  fullName: string;
  email: string;
  password: string;
  termsAccepted: boolean;
};

export type UserReactivateAccountDTO = {
  cpf: string;
  email: string;
  password: string;
};

export type SuspendedPasswordChangeDTO = {
  cpf: string;
  email: string;
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
};

async function authenticate(request: LoginDTO): Promise<TokenJWTDTO> {
  return apiRequest<TokenJWTDTO>({ method: "POST", url: "/user/login", data: request });
}

export async function registerUser(request: UserRegisterDTO): Promise<AuthSession> {
  // O cadastro é público. Limpa qualquer JWT antigo para impedir que o
  // SecurityFilter do backend tente validar um token inválido nessa rota.
  clearAuthSession();

  await apiRequest<void>({
    method: "POST",
    url: "/user/register",
    data: {
      ...request,
      cpf: request.cpf.replace(/\D/g, ""),
      fullName: request.fullName.trim(),
      email: request.email.trim().toLowerCase(),
    },
  });

  return loginUser({ email: request.email, password: request.password });
}

export async function loginUser(request: LoginDTO): Promise<AuthSession> {
  const tokenResponse = await authenticate(request);
  saveAccessToken(tokenResponse.token);

  try {
    const user = await getCurrentUser();
    return saveAuthSession(tokenResponse.token, { user, access: "USER" });
  } catch (error) {
    clearAuthSession();
    throw error;
  }
}

export async function loginAdmin(request: LoginDTO): Promise<AuthSession> {
  const tokenResponse = await authenticate(request);
  saveAccessToken(tokenResponse.token);

  try {
    // O backend usa o mesmo login para USER e ADMIN. A autorização é confirmada
    // por uma chamada a um recurso /admin/**, pois o TokenJWTDTO não expõe a role.
    await apiRequest<unknown[]>({ method: "GET", url: "/admin/reports" });
    const user = await getCurrentUser();
    return saveAuthSession(tokenResponse.token, { user, access: "ADMIN" });
  } catch (error) {
    clearAuthSession();
    if (error instanceof ApiError && error.status === 403) {
      throw new ApiError("A conta autenticada não possui a role ADMIN.", 403, error.response);
    }
    throw error;
  }
}

export async function deleteOwnAccount(): Promise<void> {
  await apiRequest<void>({ method: "DELETE", url: "/user/me" });
  clearAuthSession();
}

export async function reactivateAccount(request: UserReactivateAccountDTO): Promise<void> {
  await apiRequest<void>({ method: "POST", url: "/user/reactivate", data: request });
}

export async function changeSuspendedPassword(request: SuspendedPasswordChangeDTO): Promise<void> {
  await apiRequest<void>({ method: "PATCH", url: "/user/suspended/password", data: {
    ...request,
    cpf: request.cpf.replace(/\D/g, ""),
    email: request.email.trim().toLowerCase(),
  } });
}

export { currentSession };

export function logout(): void {
  clearAuthSession();
}
