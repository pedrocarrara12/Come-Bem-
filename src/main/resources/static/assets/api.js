const jsonHeaders = {
    "Content-Type": "application/json",
    "Accept": "application/json"
};

async function request(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: options.body ? jsonHeaders : { "Accept": "application/json" }
    });

    if (!response.ok) {
        let message = "Não foi possível concluir a operação.";
        try {
            const error = await response.json();
            message = error.mensagem || message;
        } catch (ignored) {
            message = response.status === 404
                ? "Serviço indisponível ou recurso não encontrado."
                : message;
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function body(value) {
    return JSON.stringify(value);
}

export const api = {
    clientes: {
        listar: () => request("/clientes"),
        criar: (data) => request("/clientes", { method: "POST", body: body(data) }),
        atualizar: (id, data) => request(`/clientes/${id}`, { method: "PUT", body: body(data) }),
        excluir: (id) => request(`/clientes/${id}`, { method: "DELETE" })
    },
    mesas: {
        listar: () => request("/mesas"),
        criar: (data) => request("/mesas", { method: "POST", body: body(data) }),
        atualizar: (id, data) => request(`/mesas/${id}`, { method: "PUT", body: body(data) }),
        status: (id, status) => request(`/mesas/${id}/status`, {
            method: "PATCH",
            body: body({ status })
        }),
        excluir: (id) => request(`/mesas/${id}`, { method: "DELETE" })
    },
    produtos: {
        listar: () => request("/produtos"),
        criar: (data) => request("/produtos", { method: "POST", body: body(data) }),
        atualizar: (id, data) => request(`/produtos/${id}`, { method: "PUT", body: body(data) }),
        ativar: (id) => request(`/produtos/${id}/ativar`, { method: "PATCH" }),
        inativar: (id) => request(`/produtos/${id}/inativar`, { method: "PATCH" }),
        excluir: (id) => request(`/produtos/${id}`, { method: "DELETE" })
    },
    pedidos: {
        listar: () => request("/pedidos"),
        criar: (data) => request("/pedidos", { method: "POST", body: body(data) }),
        status: (id, status) => request(`/pedidos/${id}/status`, {
            method: "PATCH",
            body: body({ status })
        }),
        adicionarItem: (id, data) => request(`/pedidos/${id}/itens`, {
            method: "POST",
            body: body(data)
        }),
        removerItem: (id, itemId) => request(`/pedidos/${id}/itens/${itemId}`, {
            method: "DELETE"
        }),
        cancelar: (id) => request(`/pedidos/${id}/cancelar`, { method: "POST" }),
        finalizar: (id) => request(`/pedidos/${id}/finalizar`, { method: "POST" })
    }
};
