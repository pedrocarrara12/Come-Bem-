import { api } from "./api.js";

const state = {
    clientes: [],
    mesas: [],
    produtos: [],
    pedidos: [],
    section: "dashboard",
    orderFilter: "TODOS"
};

const titles = {
    dashboard: "Visão geral",
    pedidos: "Pedidos",
    mesas: "Mesas",
    produtos: "Cardápio",
    clientes: "Clientes"
};

const statusLabels = {
    ABERTO: "Aberto",
    EM_PREPARO: "Em preparo",
    PRONTO: "Pronto",
    ENTREGUE: "Entregue",
    CANCELADO: "Cancelado",
    FINALIZADO: "Finalizado",
    LIVRE: "Livre",
    OCUPADA: "Ocupada",
    RESERVADA: "Reservada",
    INATIVA: "Inativa"
};

const categoryLabels = {
    ENTRADA: "Entrada",
    PRATO_PRINCIPAL: "Prato principal",
    BEBIDA: "Bebida",
    SOBREMESA: "Sobremesa",
    OUTROS: "Outros"
};

const nextStatus = {
    ABERTO: "EM_PREPARO",
    EM_PREPARO: "PRONTO",
    PRONTO: "ENTREGUE",
    ENTREGUE: "FINALIZADO"
};

const nextStatusAction = {
    ABERTO: "Enviar à cozinha",
    EM_PREPARO: "Marcar pronto",
    PRONTO: "Marcar entregue",
    ENTREGUE: "Finalizar"
};

const currency = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL"
});

const dateTime = new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short"
});

const elements = {
    apiState: document.querySelector("#api-state"),
    title: document.querySelector("#page-title"),
    contextAction: document.querySelector("#context-action"),
    metrics: document.querySelector("#metrics"),
    recentOrders: document.querySelector("#recent-orders"),
    tableMap: document.querySelector("#table-map"),
    orders: document.querySelector("#orders-grid"),
    mesas: document.querySelector("#mesas-grid"),
    products: document.querySelector("#products-table"),
    clients: document.querySelector("#clients-table"),
    modal: document.querySelector("#form-modal"),
    form: document.querySelector("#entity-form"),
    toastRegion: document.querySelector("#toast-region")
};

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function statusBadge(status) {
    return `<span class="status ${status}">${statusLabels[status] || status}</span>`;
}

function setApiStatus(online) {
    elements.apiState.classList.toggle("online", online);
    elements.apiState.classList.toggle("offline", !online);
    elements.apiState.innerHTML = `<i></i>${online ? "API conectada" : "API indisponível"}`;
}

function empty(message) {
    return `<div class="empty">${escapeHtml(message)}</div>`;
}

function toast(message, type = "success") {
    const item = document.createElement("div");
    item.className = `toast ${type === "error" ? "error" : ""}`;
    item.textContent = message;
    elements.toastRegion.appendChild(item);
    window.setTimeout(() => item.remove(), 3600);
}

async function carregarDados(showFeedback = false) {
    try {
        const [clientes, mesas, produtos, pedidos] = await Promise.all([
            api.clientes.listar(),
            api.mesas.listar(),
            api.produtos.listar(),
            api.pedidos.listar()
        ]);
        Object.assign(state, { clientes, mesas, produtos, pedidos });
        setApiStatus(true);
        renderAll();
        if (showFeedback) {
            toast("Dados atualizados.");
        }
    } catch (error) {
        setApiStatus(false);
        renderAll();
        if (showFeedback) {
            toast(error.message, "error");
        }
    }
}

function renderAll() {
    renderMetrics();
    renderRecentOrders();
    renderTableMap();
    renderOrders();
    renderMesas();
    renderProdutos();
    renderClientes();
}

function renderMetrics() {
    const ativos = state.pedidos.filter((pedido) =>
        !["FINALIZADO", "CANCELADO"].includes(pedido.status)
    );
    const faturamento = state.pedidos
        .filter((pedido) => pedido.status === "FINALIZADO")
        .reduce((total, pedido) => total + Number(pedido.valorTotal), 0);
    const ocupadas = state.mesas.filter((mesa) => mesa.status === "OCUPADA").length;
    const produtosAtivos = state.produtos.filter((produto) => produto.ativo).length;

    const metrics = [
        ["Pedidos ativos", ativos.length, "Em atendimento agora"],
        ["Mesas ocupadas", ocupadas, `${state.mesas.length} mesas cadastradas`],
        ["Cardápio ativo", produtosAtivos, "Produtos disponíveis"],
        ["Receita finalizada", currency.format(faturamento), "Pedidos encerrados"]
    ];

    elements.metrics.innerHTML = metrics.map(([label, value, note]) => `
        <article class="metric">
            <span>${label}</span>
            <strong>${value}</strong>
            <em>${note}</em>
        </article>
    `).join("");
}

function renderRecentOrders() {
    const pedidos = [...state.pedidos]
        .sort((a, b) => (b.id || 0) - (a.id || 0))
        .slice(0, 4);
    elements.recentOrders.innerHTML = pedidos.length ? pedidos.map((pedido) => `
        <div class="recent-row">
            ${statusBadge(pedido.status)}
            <div>
                <strong>Pedido #${pedido.id} · Mesa ${pedido.mesaNumero}</strong>
                <small>${escapeHtml(pedido.clienteNome)}</small>
            </div>
            <span class="amount">${currency.format(pedido.valorTotal)}</span>
        </div>
    `).join("") : empty("Nenhum pedido cadastrado ainda.");
}

function renderTableMap() {
    elements.tableMap.innerHTML = state.mesas.length ? state.mesas.map((mesa) => `
        <div class="table-seat">
            <strong>Mesa ${mesa.numero}</strong>
            ${statusBadge(mesa.status)}
        </div>
    `).join("") : empty("Cadastre mesas para visualizar o salão.");
}

function renderOrders() {
    const pedidos = state.orderFilter === "TODOS"
        ? state.pedidos
        : state.pedidos.filter((pedido) => pedido.status === state.orderFilter);
    const sorted = [...pedidos].sort((a, b) => (b.id || 0) - (a.id || 0));

    elements.orders.innerHTML = sorted.length ? sorted.map((pedido) => {
        const editavel = !["FINALIZADO", "CANCELADO"].includes(pedido.status);
        const proximo = nextStatus[pedido.status];
        const podeCancelar = ["ABERTO", "EM_PREPARO"].includes(pedido.status);
        return `
            <article class="order-card">
                <div class="card-head">
                    <div>
                        <h3>Pedido #${pedido.id}</h3>
                        <p>Mesa ${pedido.mesaNumero} · ${escapeHtml(pedido.clienteNome)}</p>
                    </div>
                    ${statusBadge(pedido.status)}
                </div>
                <div class="item-list">
                    ${pedido.itens.map((item) => `
                        <div class="item-line">
                            <span>${item.quantidade}x ${escapeHtml(item.produtoNome)}</span>
                            <span>
                                ${currency.format(item.subtotal)}
                                ${editavel ? `<button class="icon-button" data-remove-item="${pedido.id}" data-item-id="${item.id}">Remover</button>` : ""}
                            </span>
                        </div>
                    `).join("")}
                </div>
                <div class="total-line">
                    <span>Total</span>
                    <span>${currency.format(pedido.valorTotal)}</span>
                </div>
                <div class="card-actions">
                    ${proximo ? `<button class="button button-primary button-small" data-order-status="${pedido.id}" data-status="${proximo}">${nextStatusAction[pedido.status]}</button>` : ""}
                    ${editavel ? `<button class="button button-ghost button-small" data-add-item="${pedido.id}">Adicionar item</button>` : ""}
                    ${podeCancelar ? `<button class="button button-danger button-small" data-cancel-order="${pedido.id}">Cancelar</button>` : ""}
                </div>
            </article>
        `;
    }).join("") : empty("Nenhum pedido encontrado neste filtro.");
}

function renderMesas() {
    elements.mesas.innerHTML = state.mesas.length ? state.mesas.map((mesa) => `
        <article class="entity-card">
            <div class="card-head">
                <div class="number">${String(mesa.numero).padStart(2, "0")}</div>
                ${statusBadge(mesa.status)}
            </div>
            <p class="meta">Capacidade para ${mesa.capacidade} pessoas</p>
            <div class="card-actions">
                <button class="button button-ghost button-small" data-edit-mesa="${mesa.id}">Editar</button>
                <button class="button button-danger button-small" data-delete-mesa="${mesa.id}">Excluir</button>
            </div>
        </article>
    `).join("") : empty("Nenhuma mesa cadastrada.");
}

function renderProdutos() {
    elements.products.innerHTML = state.produtos.length ? state.produtos.map((produto) => `
        <tr>
            <td class="table-name">
                <strong>${escapeHtml(produto.nome)}</strong>
                <small>${escapeHtml(produto.descricao || "Sem descrição")}</small>
            </td>
            <td>${categoryLabels[produto.categoria] || produto.categoria}</td>
            <td>${currency.format(produto.preco)}</td>
            <td>${statusBadge(produto.ativo ? "LIVRE" : "INATIVA").replace("Livre", "Ativo").replace("Inativa", "Inativo")}</td>
            <td class="row-actions">
                <button class="icon-button" data-toggle-produto="${produto.id}" data-ativo="${produto.ativo}">${produto.ativo ? "Inativar" : "Ativar"}</button>
                <button class="icon-button" data-edit-produto="${produto.id}">Editar</button>
                <button class="icon-button" data-delete-produto="${produto.id}">Excluir</button>
            </td>
        </tr>
    `).join("") : `<tr><td colspan="5">${empty("Nenhum produto cadastrado.")}</td></tr>`;
}

function renderClientes() {
    elements.clients.innerHTML = state.clientes.length ? state.clientes.map((cliente) => `
        <tr>
            <td class="table-name">
                <strong>${escapeHtml(cliente.nome)}</strong>
                <small>${escapeHtml(cliente.email || "Sem e-mail")}</small>
            </td>
            <td>${escapeHtml(cliente.cpf)}</td>
            <td>${escapeHtml(cliente.telefone || "-")}</td>
            <td>${cliente.pedidoIds.length}</td>
            <td class="row-actions">
                <button class="icon-button" data-edit-cliente="${cliente.id}">Editar</button>
                <button class="icon-button" data-delete-cliente="${cliente.id}">Excluir</button>
            </td>
        </tr>
    `).join("") : `<tr><td colspan="5">${empty("Nenhum cliente cadastrado.")}</td></tr>`;
}

function navigate(section) {
    state.section = section;
    elements.title.textContent = titles[section];
    document.querySelectorAll(".nav-item").forEach((item) => {
        item.classList.toggle("active", item.dataset.section === section);
    });
    document.querySelectorAll(".view").forEach((view) => {
        view.classList.toggle("active", view.id === `${section}-view`);
    });
    const context = {
        dashboard: ["Novo pedido", "pedido"],
        pedidos: ["Novo pedido", "pedido"],
        mesas: ["Cadastrar mesa", "mesa"],
        produtos: ["Novo produto", "produto"],
        clientes: ["Novo cliente", "cliente"]
    }[section];
    elements.contextAction.textContent = context[0];
    elements.contextAction.dataset.openForm = context[1];
}

function modal(title, subtitle, content, type, id = "") {
    elements.form.dataset.type = type;
    elements.form.dataset.id = id;
    elements.form.innerHTML = `
        <header class="modal-header">
            <div>
                <p class="eyebrow">${escapeHtml(subtitle)}</p>
                <h2>${escapeHtml(title)}</h2>
            </div>
            <button type="button" class="close-modal" aria-label="Fechar">&times;</button>
        </header>
        <div class="modal-body">${content}</div>
        <footer class="modal-footer">
            <button type="button" class="button button-ghost close-modal">Cancelar</button>
            <button type="submit" class="button button-primary">Salvar</button>
        </footer>
    `;
    elements.modal.showModal();
}

function clienteForm(cliente = {}) {
    modal(
        cliente.id ? "Editar cliente" : "Novo cliente",
        "Clientes",
        `<div class="form-grid">
            <div class="field full">
                <label for="nome">Nome completo</label>
                <input id="nome" name="nome" required minlength="3" maxlength="120" value="${escapeHtml(cliente.nome)}">
            </div>
            <div class="field">
                <label for="cpf">CPF</label>
                <input id="cpf" name="cpf" required pattern="[0-9]{11}" maxlength="11" value="${escapeHtml(cliente.cpf)}">
            </div>
            <div class="field">
                <label for="telefone">Telefone</label>
                <input id="telefone" name="telefone" value="${escapeHtml(cliente.telefone)}">
            </div>
            <div class="field full">
                <label for="email">E-mail</label>
                <input id="email" name="email" type="email" value="${escapeHtml(cliente.email)}">
            </div>
        </div>`,
        "cliente",
        cliente.id
    );
}

function mesaForm(mesa = {}) {
    modal(
        mesa.id ? "Editar mesa" : "Cadastrar mesa",
        "Salão",
        `<div class="form-grid">
            <div class="field">
                <label for="numero">Número</label>
                <input id="numero" name="numero" type="number" min="1" required value="${escapeHtml(mesa.numero)}">
            </div>
            <div class="field">
                <label for="capacidade">Capacidade</label>
                <input id="capacidade" name="capacidade" type="number" min="1" required value="${escapeHtml(mesa.capacidade || 4)}">
            </div>
            <div class="field full">
                <label for="status">Status inicial</label>
                <select id="status" name="status">
                    ${["LIVRE", "RESERVADA", "INATIVA"].map((value) =>
                        `<option value="${value}" ${mesa.status === value ? "selected" : ""}>${statusLabels[value]}</option>`
                    ).join("")}
                </select>
            </div>
        </div>`,
        "mesa",
        mesa.id
    );
}

function produtoForm(produto = {}) {
    modal(
        produto.id ? "Editar produto" : "Novo produto",
        "Cardápio",
        `<div class="form-grid">
            <div class="field full">
                <label for="nome">Nome</label>
                <input id="nome" name="nome" required maxlength="120" value="${escapeHtml(produto.nome)}">
            </div>
            <div class="field full">
                <label for="descricao">Descrição</label>
                <textarea id="descricao" name="descricao" maxlength="1000">${escapeHtml(produto.descricao)}</textarea>
            </div>
            <div class="field">
                <label for="preco">Preço</label>
                <input id="preco" name="preco" type="number" step="0.01" min="0.01" required value="${escapeHtml(produto.preco)}">
            </div>
            <div class="field">
                <label for="categoria">Categoria</label>
                <select id="categoria" name="categoria" required>
                    ${Object.entries(categoryLabels).map(([value, label]) =>
                        `<option value="${value}" ${produto.categoria === value ? "selected" : ""}>${label}</option>`
                    ).join("")}
                </select>
            </div>
        </div>`,
        "produto",
        produto.id
    );
}

function productOptions() {
    return state.produtos
        .filter((produto) => produto.ativo)
        .map((produto) => `<option value="${produto.id}">${escapeHtml(produto.nome)} · ${currency.format(produto.preco)}</option>`)
        .join("");
}

function itemRow() {
    return `
        <div class="order-item-editor">
            <select name="produtoId" required>${productOptions()}</select>
            <input name="quantidade" type="number" min="1" value="1" required aria-label="Quantidade">
            <button type="button" class="remove-line" aria-label="Remover item">&times;</button>
        </div>
    `;
}

function pedidoForm() {
    if (!state.clientes.length || !state.mesas.length || !state.produtos.some((produto) => produto.ativo)) {
        toast("Cadastre cliente, mesa e produto ativo antes de abrir um pedido.", "error");
        return;
    }
    modal(
        "Abrir novo pedido",
        "Atendimento",
        `<div class="form-grid">
            <div class="field">
                <label for="clienteId">Cliente</label>
                <select id="clienteId" name="clienteId" required>
                    ${state.clientes.map((cliente) => `<option value="${cliente.id}">${escapeHtml(cliente.nome)}</option>`).join("")}
                </select>
            </div>
            <div class="field">
                <label for="mesaId">Mesa</label>
                <select id="mesaId" name="mesaId" required>
                    ${state.mesas.filter((mesa) => mesa.status !== "INATIVA").map((mesa) =>
                        `<option value="${mesa.id}">Mesa ${mesa.numero} · ${statusLabels[mesa.status]}</option>`
                    ).join("")}
                </select>
            </div>
            <div class="field full" id="items-editor">
                <label>Itens do pedido</label>
                ${itemRow()}
                <button type="button" class="add-line">+ Adicionar produto</button>
            </div>
        </div>`,
        "pedido"
    );
}

function adicionarItemForm(pedidoId) {
    modal(
        "Adicionar item",
        `Pedido #${pedidoId}`,
        `<div class="form-grid">
            <div class="field full">
                <label for="produtoId">Produto</label>
                <select id="produtoId" name="produtoId" required>${productOptions()}</select>
            </div>
            <div class="field">
                <label for="quantidade">Quantidade</label>
                <input id="quantidade" name="quantidade" type="number" min="1" value="1" required>
            </div>
        </div>`,
        "item",
        pedidoId
    );
}

function openForm(type, id) {
    if (type === "cliente") {
        clienteForm(state.clientes.find((cliente) => cliente.id === Number(id)));
    } else if (type === "mesa") {
        mesaForm(state.mesas.find((mesa) => mesa.id === Number(id)));
    } else if (type === "produto") {
        produtoForm(state.produtos.find((produto) => produto.id === Number(id)));
    } else if (type === "pedido") {
        pedidoForm();
    }
}

async function submitForm(event) {
    event.preventDefault();
    const data = new FormData(elements.form);
    const id = elements.form.dataset.id;
    const type = elements.form.dataset.type;
    try {
        if (type === "cliente") {
            const dto = {
                nome: data.get("nome"),
                cpf: data.get("cpf"),
                telefone: data.get("telefone") || null,
                email: data.get("email") || null
            };
            id ? await api.clientes.atualizar(id, dto) : await api.clientes.criar(dto);
        } else if (type === "mesa") {
            const dto = {
                numero: Number(data.get("numero")),
                capacidade: Number(data.get("capacidade")),
                status: data.get("status")
            };
            id ? await api.mesas.atualizar(id, dto) : await api.mesas.criar(dto);
        } else if (type === "produto") {
            const atual = state.produtos.find((produto) => produto.id === Number(id));
            const dto = {
                nome: data.get("nome"),
                descricao: data.get("descricao") || null,
                preco: Number(data.get("preco")),
                categoria: data.get("categoria"),
                ativo: atual ? atual.ativo : true
            };
            id ? await api.produtos.atualizar(id, dto) : await api.produtos.criar(dto);
        } else if (type === "pedido") {
            const produtos = data.getAll("produtoId");
            const quantidades = data.getAll("quantidade");
            await api.pedidos.criar({
                clienteId: Number(data.get("clienteId")),
                mesaId: Number(data.get("mesaId")),
                itens: produtos.map((produtoId, index) => ({
                    produtoId: Number(produtoId),
                    quantidade: Number(quantidades[index])
                }))
            });
        } else if (type === "item") {
            await api.pedidos.adicionarItem(id, {
                produtoId: Number(data.get("produtoId")),
                quantidade: Number(data.get("quantidade"))
            });
        }
        elements.modal.close();
        await carregarDados();
        toast("Operação realizada com sucesso.");
    } catch (error) {
        toast(error.message, "error");
    }
}

async function runAction(action) {
    try {
        await action();
        await carregarDados();
        toast("Operação realizada com sucesso.");
    } catch (error) {
        toast(error.message, "error");
    }
}

document.addEventListener("click", (event) => {
    const target = event.target.closest("button");
    if (!target) {
        return;
    }
    if (target.dataset.section) {
        navigate(target.dataset.section);
    } else if (target.dataset.goTo) {
        navigate(target.dataset.goTo);
    } else if (target.dataset.openForm) {
        openForm(target.dataset.openForm);
    } else if (target.dataset.editCliente) {
        openForm("cliente", target.dataset.editCliente);
    } else if (target.dataset.editMesa) {
        openForm("mesa", target.dataset.editMesa);
    } else if (target.dataset.editProduto) {
        openForm("produto", target.dataset.editProduto);
    } else if (target.dataset.addItem) {
        adicionarItemForm(target.dataset.addItem);
    } else if (target.classList.contains("close-modal")) {
        elements.modal.close();
    } else if (target.classList.contains("add-line")) {
        target.insertAdjacentHTML("beforebegin", itemRow());
    } else if (target.classList.contains("remove-line")) {
        const editor = target.closest("#items-editor");
        if (editor.querySelectorAll(".order-item-editor").length > 1) {
            target.parentElement.remove();
        }
    } else if (target.dataset.filter) {
        state.orderFilter = target.dataset.filter;
        document.querySelectorAll("#order-filters .chip").forEach((chip) => {
            chip.classList.toggle("active", chip.dataset.filter === state.orderFilter);
        });
        renderOrders();
    } else if (target.dataset.orderStatus) {
        const pedidoId = target.dataset.orderStatus;
        const status = target.dataset.status;
        runAction(() => status === "FINALIZADO"
            ? api.pedidos.finalizar(pedidoId)
            : api.pedidos.status(pedidoId, status));
    } else if (target.dataset.cancelOrder) {
        runAction(() => api.pedidos.cancelar(target.dataset.cancelOrder));
    } else if (target.dataset.removeItem && window.confirm("Remover este item do pedido?")) {
        runAction(() => api.pedidos.removerItem(target.dataset.removeItem, target.dataset.itemId));
    } else if (target.dataset.toggleProduto) {
        runAction(() => target.dataset.ativo === "true"
            ? api.produtos.inativar(target.dataset.toggleProduto)
            : api.produtos.ativar(target.dataset.toggleProduto));
    } else if (target.dataset.deleteCliente && window.confirm("Excluir este cliente?")) {
        runAction(() => api.clientes.excluir(target.dataset.deleteCliente));
    } else if (target.dataset.deleteMesa && window.confirm("Excluir esta mesa?")) {
        runAction(() => api.mesas.excluir(target.dataset.deleteMesa));
    } else if (target.dataset.deleteProduto && window.confirm("Excluir este produto? Se ele já foi usado em pedidos, será inativado.")) {
        runAction(() => api.produtos.excluir(target.dataset.deleteProduto));
    }
});

elements.form.addEventListener("submit", submitForm);
document.querySelector("#refresh-button").addEventListener("click", () => carregarDados(true));
elements.contextAction.addEventListener("click", () => openForm(elements.contextAction.dataset.openForm || "pedido"));

navigate("dashboard");
renderAll();
carregarDados();
