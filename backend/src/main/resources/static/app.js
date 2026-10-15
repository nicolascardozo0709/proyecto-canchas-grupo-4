/**
 * LÓGICA DE CLIENTE WEB - PLATAFORMA DE RESERVAS DE CANCHAS (ENTREGA 2)
 * Implementa los flujos interactivos de los Wireframes WF-01, WF-02, WF-03 y Panel de Admin
 */

const API_BASE = '/api';

const app = {
    user: null,
    token: null,
    escenarios: [],
    currentEscenario: null,
    selectedFranja: null,
    selectedFecha: null,
    filtroDisciplina: '',

    init() {
        // Cargar token guardado si existe
        this.token = localStorage.getItem('token_canchas');
        const userJson = localStorage.getItem('user_canchas');
        if (userJson) {
            try { this.user = JSON.parse(userJson); } catch (e) { this.user = null; }
        }

        this.updateNavUser();
        this.cargarEscenarios();
        if (this.token) {
            this.cargarMisReservas();
        }

        // Configurar fecha mínima de reserva a hoy (HU-08)
        const hoy = new Date().toISOString().split('T')[0];
        const inputFecha = document.getElementById('fecha-reserva');
        if (inputFecha) {
            inputFecha.min = hoy;
            inputFecha.value = hoy;
            this.selectedFecha = hoy;
        }
    },

    // -------------------------------------------------------------
    // GESTIÓN DE SESIÓN Y AUTENTICACIÓN
    // -------------------------------------------------------------
    updateNavUser() {
        const container = document.getElementById('nav-user-container');
        const adminBtn = document.getElementById('nav-admin');
        const adminBar = document.getElementById('admin-indicator-bar');

        if (this.user && this.token) {
            const esAdmin = this.user.rol === 'ROLE_ADMIN';
            if (adminBtn) adminBtn.style.display = esAdmin ? 'flex' : 'none';
            if (adminBar) adminBar.style.display = esAdmin ? 'block' : 'none';

            container.innerHTML = `
                <div style="display:flex; align-items:center; gap:10px;">
                    <div style="text-align:right;">
                        <div style="font-weight:700; font-size:0.88rem; ${esAdmin ? 'color:#d97706;' : ''}">
                            ${esAdmin ? '<i class="fa-solid fa-crown"></i> ' : ''}${this.user.nombre} ${this.user.apellido}
                        </div>
                        <span class="badge ${esAdmin ? 'badge-mantenimiento' : 'badge-primary'}" style="position:static; font-size:0.65rem; ${esAdmin ? 'background:#f59e0b; color:#0f172a; font-weight:800;' : ''}">
                            ${esAdmin ? 'ADMINISTRADOR CAMPUS' : 'DEPORTISTA'}
                        </span>
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="app.logout()">
                        <i class="fa-solid fa-right-from-bracket"></i> Salir
                    </button>
                </div>
            `;
        } else {
            if (adminBtn) adminBtn.style.display = 'none';
            if (adminBar) adminBar.style.display = 'none';
            container.innerHTML = `
                <button class="btn btn-outline" onclick="app.openAuthModal('login')">
                    <i class="fa-solid fa-arrow-right-to-bracket"></i> Iniciar Sesión
                </button>
                <button class="btn btn-primary" onclick="app.openAuthModal('register')">
                    <i class="fa-solid fa-user-plus"></i> Registrarse
                </button>
            `;
        }
    },

    openAuthModal(mode = 'login') {
        const modal = document.getElementById('modal-auth');
        modal.style.display = 'flex';
        this.switchAuthMode(mode);
    },

    switchAuthMode(mode) {
        const formLogin = document.getElementById('form-login');
        const formRegister = document.getElementById('form-register');
        const title = document.getElementById('auth-modal-title');
        const subtitle = document.getElementById('auth-modal-subtitle');

        if (mode === 'login') {
            formLogin.style.display = 'block';
            formRegister.style.display = 'none';
            title.textContent = 'Iniciar Sesión';
            subtitle.textContent = 'Accede para reservar escenarios deportivos';
        } else {
            formLogin.style.display = 'none';
            formRegister.style.display = 'block';
            title.textContent = 'Crear Cuenta de Deportista';
            subtitle.textContent = 'Únete al complejo deportivo Javeriana';
        }
    },

    fillDemo(email, pass) {
        document.getElementById('login-email').value = email;
        document.getElementById('login-password').value = pass;
    },

    async handleLogin(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Error al iniciar sesión');

            this.token = data.token;
            this.user = {
                id: data.id,
                nombre: data.nombre,
                apellido: data.apellido,
                email: data.email,
                rol: data.rol
            };

            localStorage.setItem('token_canchas', this.token);
            localStorage.setItem('user_canchas', JSON.stringify(this.user));

            this.closeModal('modal-auth');
            this.updateNavUser();
            this.cargarMisReservas();
            this.showToast(`¡Bienvenido, ${this.user.nombre}!`, 'success');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        const body = {
            nombre: document.getElementById('reg-nombre').value,
            apellido: document.getElementById('reg-apellido').value,
            email: document.getElementById('reg-email').value,
            telefono: document.getElementById('reg-telefono').value,
            password: document.getElementById('reg-password').value,
            rol: document.getElementById('reg-rol').value
        };

        try {
            const res = await fetch(`${API_BASE}/auth/registro`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Error en el registro');

            this.token = data.token;
            this.user = {
                id: data.id,
                nombre: data.nombre,
                apellido: data.apellido,
                email: data.email,
                rol: data.rol
            };

            localStorage.setItem('token_canchas', this.token);
            localStorage.setItem('user_canchas', JSON.stringify(this.user));

            this.closeModal('modal-auth');
            this.updateNavUser();
            this.showToast('¡Cuenta creada y autenticada con éxito!', 'success');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    logout() {
        this.token = null;
        this.user = null;
        localStorage.removeItem('token_canchas');
        localStorage.removeItem('user_canchas');
        this.updateNavUser();
        this.showSection('catalogo');
        this.showToast('Has cerrado sesión correctamente.', 'info');
    },

    // -------------------------------------------------------------
    // NAVEGACIÓN Y SECCIONES
    // -------------------------------------------------------------
    showSection(sectionId) {
        document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
        document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));

        const target = document.getElementById(`section-${sectionId}`);
        if (target) target.classList.add('active');

        const navBtn = document.getElementById(`nav-${sectionId}`);
        if (navBtn) navBtn.classList.add('active');

        if (sectionId === 'mis-reservas') {
            if (!this.token) {
                this.openAuthModal('login');
                this.showToast('Por favor inicia sesión para ver tus reservas.', 'warning');
                return;
            }
            this.cargarMisReservas();
        } else if (sectionId === 'admin') {
            this.cargarTablaAdmin();
        }
    },

    // -------------------------------------------------------------
    // WF-01: CATÁLOGO DE ESCENARIOS Y FILTROS
    // -------------------------------------------------------------
    async cargarEscenarios() {
        try {
            let url = `${API_BASE}/escenarios`;
            if (this.filtroDisciplina) {
                url += `?disciplina=${this.filtroDisciplina}`;
            }

            const res = await fetch(url);
            this.escenarios = await res.json();
            this.renderCatalogo();
        } catch (err) {
            console.error('Error cargando escenarios:', err);
        }
    },

    filterDisciplina(disc) {
        this.filtroDisciplina = disc;
        document.querySelectorAll('.pill').forEach(p => {
            if (p.getAttribute('data-disciplina') === disc) p.classList.add('active');
            else p.classList.remove('active');
        });
        this.cargarEscenarios();
    },

    renderCatalogo() {
        const grid = document.getElementById('escenarios-grid');
        const countSpan = document.getElementById('catalogo-total');
        countSpan.textContent = `(${this.escenarios.length} encontradas)`;

        if (this.escenarios.length === 0) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align:center; padding: 48px; color: var(--text-muted);">
                <i class="fa-solid fa-circle-exclamation" style="font-size:2rem; margin-bottom:12px;"></i>
                <p>No se encontraron escenarios disponibles para esta disciplina.</p>
            </div>`;
            return;
        }

        grid.innerHTML = this.escenarios.map(esc => {
            const precioFormatted = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(esc.tarifaHora);
            const enMantenimiento = esc.enMantenimiento;

            return `
                <div class="cancha-card">
                    <div class="card-image-box">
                        <img src="${esc.imagenUrl || 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=800'}" alt="${esc.nombre}">
                        <span class="badge badge-tag">${esc.disciplina}</span>
                        ${enMantenimiento ? '<span class="badge badge-mantenimiento"><i class="fa-solid fa-wrench"></i> MANTENIMIENTO</span>' : ''}
                    </div>
                    <div class="card-body">
                        <h3 class="card-title">${esc.nombre}</h3>
                        <p class="card-desc">${esc.descripcion || 'Escenario deportivo equipado con especificaciones de alto nivel.'}</p>
                        
                        <div class="card-features">
                            <div class="feature-pill">
                                <i class="fa-solid fa-layer-group"></i>
                                <span>Superficie</span>
                                <b>${esc.superficie}</b>
                            </div>
                            <div class="feature-pill">
                                <i class="fa-solid fa-users"></i>
                                <span>Aforo</span>
                                <b>${esc.aforo} pers.</b>
                            </div>
                            <div class="feature-pill">
                                <i class="fa-solid fa-lightbulb"></i>
                                <span>Iluminación</span>
                                <b>${esc.iluminacion ? 'Sí (LED)' : 'No'}</b>
                            </div>
                        </div>

                        <div class="card-footer">
                            <div class="card-price">
                                <span class="price-label">Tarifa por hora</span>
                                <span class="price-value">${precioFormatted}</span>
                            </div>
                            ${enMantenimiento ? `
                                <button class="btn btn-outline btn-sm" disabled style="opacity:0.6; cursor:not-allowed;">
                                    <i class="fa-solid fa-lock"></i> Mantenimiento
                                </button>
                            ` : `
                                <button class="btn btn-primary" onclick="app.iniciarFlujoReserva(${esc.id})">
                                    <i class="fa-solid fa-calendar-plus"></i> Reservar
                                </button>
                            `}
                        </div>

                        ${this.user && this.user.rol === 'ROLE_ADMIN' ? `
                            <div class="admin-card-actions">
                                <button class="btn btn-sm ${enMantenimiento ? 'btn-success' : 'btn-outline'}" style="flex:1; font-size:0.75rem;" onclick="app.toggleMantenimiento(${esc.id}, ${!enMantenimiento})">
                                    <i class="fa-solid fa-wrench"></i> ${enMantenimiento ? 'Habilitar' : 'Poner en Mantenimiento'}
                                </button>
                                <button class="btn btn-sm btn-outline" style="padding:6px 10px;" onclick="app.abrirModalEditarEscenario(${esc.id})" title="Editar Escenario">
                                    <i class="fa-solid fa-pen-to-square"></i>
                                </button>
                                <button class="btn btn-sm btn-danger" style="padding:6px 10px;" onclick="app.eliminarEscenario(${esc.id})" title="Eliminar Escenario">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </div>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join('');

        this.actualizarAdminKpis();
    },

    // -------------------------------------------------------------
    // WF-02: SELECTOR DE FECHA, MATRIZ DE FRANJAS Y CONFIRMACIÓN
    // -------------------------------------------------------------
    async iniciarFlujoReserva(escenarioId) {
        const esc = this.escenarios.find(e => e.id === escenarioId);
        if (!esc) return;

        this.currentEscenario = esc;
        this.selectedFranja = null;

        // Cargar datos en panel izquierdo
        document.getElementById('res-cancha-img').src = esc.imagenUrl || 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=800';
        document.getElementById('res-cancha-badge').textContent = esc.disciplina;
        document.getElementById('res-cancha-nombre').textContent = esc.nombre;
        document.getElementById('res-cancha-desc').textContent = esc.descripcion || '';
        document.getElementById('res-cancha-superficie').textContent = esc.superficie;
        document.getElementById('res-cancha-aforo').textContent = `${esc.aforo} personas`;
        document.getElementById('res-cancha-luz').textContent = esc.iluminacion ? 'Disponible' : 'No disponible';
        
        const precioFormatted = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(esc.tarifaHora);
        document.getElementById('res-cancha-precio').textContent = precioFormatted;

        // Panel de confirmación oculto inicialmente
        document.getElementById('panel-confirmacion').style.display = 'none';

        this.showSection('reserva');
        this.cargarDisponibilidad();
    },

    onFechaChange() {
        const inputFecha = document.getElementById('fecha-reserva');
        this.selectedFecha = inputFecha.value;
        this.selectedFranja = null;
        document.getElementById('panel-confirmacion').style.display = 'none';
        this.cargarDisponibilidad();
    },

    async cargarDisponibilidad() {
        if (!this.currentEscenario || !this.selectedFecha) return;

        const grid = document.getElementById('franjas-grid');
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding:24px;">Consultando disponibilidad...</div>';
        document.getElementById('matrix-fecha-subtitle').textContent = `Disponibilidad para el día ${this.selectedFecha}`;

        try {
            const res = await fetch(`${API_BASE}/reservas/disponibilidad?escenarioId=${this.currentEscenario.id}&fecha=${this.selectedFecha}`);
            const franjas = await res.json();

            if (franjas.length === 0) {
                grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding:24px; color:var(--text-muted);">No hay franjas horarias configuradas para esta cancha.</div>';
                return;
            }

            grid.innerHTML = franjas.map(f => {
                const libre = f.disponible;
                const seleccionada = this.selectedFranja && this.selectedFranja.franjaId === f.franjaId;
                const claseEstado = seleccionada ? 'seleccionada' : (libre ? 'libre' : 'ocupada');

                return `
                    <button class="franja-btn ${claseEstado}" 
                        ${libre ? `onclick="app.seleccionarFranja(${f.franjaId}, '${f.horaInicio}', '${f.horaFin}')"` : 'disabled title="Franja ya ocupada por otro usuario"'}>
                        <i class="fa-solid ${libre ? 'fa-circle-check' : 'fa-lock'}"></i>
                        <span class="franja-hora">${f.horaInicio} - ${f.horaFin}</span>
                        <span class="franja-estado">${libre ? 'Libre' : 'Ocupada'}</span>
                    </button>
                `;
            }).join('');
        } catch (err) {
            grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; color:var(--danger);">Error cargando horarios.</div>';
        }
    },

    seleccionarFranja(franjaId, horaInicio, horaFin) {
        this.selectedFranja = { franjaId, horaInicio, horaFin };
        this.cargarDisponibilidad(); // Refresca colores de la matriz

        // Mostrar Panel de Confirmación con datos
        const panel = document.getElementById('panel-confirmacion');
        panel.style.display = 'block';

        const precioFormatted = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(this.currentEscenario.tarifaHora);
        document.getElementById('confirm-resumen').textContent = `${this.currentEscenario.nombre} | ${this.selectedFecha} de ${horaInicio} a ${horaFin}`;
        document.getElementById('confirm-precio').textContent = precioFormatted;
    },

    async ejecutarReserva() {
        if (!this.token) {
            this.openAuthModal('login');
            this.showToast('Debes iniciar sesión para confirmar tu reserva.', 'warning');
            return;
        }

        if (!this.selectedFranja || !this.selectedFecha || !this.currentEscenario) {
            this.showToast('Por favor selecciona una franja horaria válida.', 'warning');
            return;
        }

        const btn = document.getElementById('btn-confirmar-reserva');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Validando Anti-Colisión...';

        try {
            const res = await fetch(`${API_BASE}/reservas`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({
                    escenarioId: this.currentEscenario.id,
                    franjaId: this.selectedFranja.franjaId,
                    fecha: this.selectedFecha
                })
            });

            const data = await res.json();

            if (res.status === 409) {
                // Conflicto de Colisión Transaccional (HU-07)
                throw new Error(data.message || 'Conflicto: Esta franja acaba de ser ocupada por otro usuario.');
            } else if (!res.ok) {
                throw new Error(data.message || 'No se pudo crear la reserva.');
            }

            this.showToast(`¡Reserva creada exitosamente! Código: ${data.codigoReserva}`, 'success');
            this.selectedFranja = null;
            document.getElementById('panel-confirmacion').style.display = 'none';
            this.cargarDisponibilidad();
            this.cargarMisReservas();
            this.showSection('mis-reservas');
        } catch (err) {
            this.showToast(err.message, 'error');
            this.cargarDisponibilidad(); // Refresca por si alguien más la ocupó
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-check-circle"></i> Confirmar Reserva Inmediata';
        }
    },

    // -------------------------------------------------------------
    // WF-03: MIS RESERVAS Y CANCELACIÓN INMEDIATA
    // -------------------------------------------------------------
    async cargarMisReservas() {
        if (!this.token) return;

        try {
            const res = await fetch(`${API_BASE}/reservas/mis-reservas`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });
            const reservas = await res.json();
            
            document.getElementById('count-reservas').textContent = reservas.filter(r => r.estado === 'CONFIRMADA').length;
            document.getElementById('badge-total-reservas').textContent = `${reservas.length} reservas`;

            const grid = document.getElementById('mis-reservas-grid');
            if (reservas.length === 0) {
                grid.innerHTML = `
                    <div style="grid-column: 1/-1; text-align:center; padding: 48px; background:white; border-radius:12px; border:1px solid var(--border);">
                        <i class="fa-solid fa-calendar-xmark" style="font-size:2.5rem; color:var(--text-muted); margin-bottom:12px;"></i>
                        <h3>Aún no tienes reservas programadas</h3>
                        <p class="text-muted">Explora nuestras canchas y agenda tus partidos fácilmente.</p>
                        <button class="btn btn-primary" style="margin-top:16px;" onclick="app.showSection('catalogo')">
                            Ver Canchas Disponibles
                        </button>
                    </div>
                `;
                return;
            }

            grid.innerHTML = reservas.map(r => {
                const esConfirmada = r.estado === 'CONFIRMADA';
                const precioFormatted = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(r.precioTotal);

                return `
                    <div class="reserva-card">
                        <div class="reserva-card-header">
                            <span class="reserva-codigo"><i class="fa-solid fa-ticket"></i> ${r.codigoReserva}</span>
                            <span class="badge ${esConfirmada ? 'badge-primary' : 'badge-mantenimiento'}">
                                ${r.estado}
                            </span>
                        </div>
                        <div class="reserva-body">
                            <h4>${r.escenarioNombre}</h4>
                            <div class="reserva-meta">
                                <div><i class="fa-solid fa-trophy"></i> Disciplina: <b>${r.disciplina}</b></div>
                                <div><i class="fa-solid fa-calendar-days"></i> Fecha del Partido: <b>${r.fecha}</b></div>
                                <div><i class="fa-solid fa-clock"></i> Horario: <b>${r.horaInicio} - ${r.horaFin}</b></div>
                                ${!esConfirmada && r.motivoCancelacion ? `<div><i class="fa-solid fa-circle-exclamation"></i> Motivo: <i>${r.motivoCancelacion}</i></div>` : ''}
                            </div>
                        </div>
                        <div class="reserva-footer">
                            <span style="font-weight:700; color:var(--primary-dark); font-size:1.05rem;">${precioFormatted}</span>
                            ${esConfirmada ? `
                                <button class="btn btn-outline btn-danger btn-sm" onclick="app.abrirModalCancelar(${r.id})">
                                    <i class="fa-solid fa-ban"></i> Cancelar Reserva
                                </button>
                            ` : `
                                <span class="text-muted" style="font-size:0.78rem;">Franja liberada</span>
                            `}
                        </div>
                    </div>
                `;
            }).join('');
        } catch (err) {
            console.error('Error cargando mis reservas:', err);
        }
    },

    abrirModalCancelar(reservaId) {
        document.getElementById('cancel-reserva-id').value = reservaId;
        document.getElementById('cancel-motivo').value = '';
        document.getElementById('modal-cancelar').style.display = 'flex';
    },

    async ejecutarCancelacion() {
        const id = document.getElementById('cancel-reserva-id').value;
        const motivo = document.getElementById('cancel-motivo').value;

        try {
            const res = await fetch(`${API_BASE}/reservas/${id}/cancelar`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ motivo })
            });

            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Error al cancelar la reserva');

            this.closeModal('modal-cancelar');
            this.showToast('Reserva cancelada exitosamente. La franja horaria se ha liberado.', 'info');
            this.cargarMisReservas();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // -------------------------------------------------------------
    // PANEL ADMINISTRATIVO (CRUD DE ESCENARIOS Y MANTENIMIENTO)
    // -------------------------------------------------------------
    async cargarTablaAdmin() {
        try {
            const res = await fetch(`${API_BASE}/escenarios`);
            const escenarios = await res.json();

            const tbody = document.getElementById('admin-escenarios-tbody');
            tbody.innerHTML = escenarios.map(esc => {
                const precio = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(esc.tarifaHora);
                return `
                    <tr>
                        <td><b>#${esc.id}</b></td>
                        <td><b>${esc.nombre}</b></td>
                        <td><span class="badge badge-tag" style="position:static;">${esc.disciplina}</span></td>
                        <td>${esc.superficie}</td>
                        <td>${esc.aforo} pers.</td>
                        <td>${precio}</td>
                        <td>
                            <span class="badge ${esc.enMantenimiento ? 'badge-mantenimiento' : 'badge-primary'}" style="position:static;">
                                ${esc.enMantenimiento ? 'EN MANTENIMIENTO' : 'OPERATIVA'}
                            </span>
                        </td>
                        <td>
                            <div style="display:flex; gap:6px;">
                                <button class="btn btn-outline btn-sm" onclick="app.toggleMantenimiento(${esc.id}, ${!esc.enMantenimiento})" title="Alternar mantenimiento (HU-21)">
                                    <i class="fa-solid ${esc.enMantenimiento ? 'fa-wrench' : 'fa-tools'}"></i>
                                </button>
                                <button class="btn btn-outline btn-sm" onclick="app.abrirModalEditarEscenario(${esc.id})" title="Editar">
                                    <i class="fa-solid fa-pen-to-square"></i>
                                </button>
                                <button class="btn btn-danger btn-sm" onclick="app.eliminarEscenario(${esc.id})" title="Eliminar (HU-03)">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');

            this.actualizarAdminKpis();
            this.cargarReservasGlobalesAdmin();
        } catch (err) {
            console.error('Error en tabla admin:', err);
        }
    },

    actualizarAdminKpis() {
        if (!this.escenarios) return;
        const total = this.escenarios.length;
        const activas = this.escenarios.filter(e => !e.enMantenimiento).length;
        const mantenimiento = this.escenarios.filter(e => e.enMantenimiento).length;
        const aforo = this.escenarios.reduce((acc, e) => acc + (e.aforo || 0), 0);

        const elTotal = document.getElementById('kpi-total-canchas');
        const elActivas = document.getElementById('kpi-canchas-activas');
        const elMant = document.getElementById('kpi-canchas-mantenimiento');
        const elAforo = document.getElementById('kpi-aforo-total');

        if (elTotal) elTotal.textContent = total;
        if (elActivas) elActivas.textContent = activas;
        if (elMant) elMant.textContent = mantenimiento;
        if (elAforo) elAforo.textContent = `${aforo} pers.`;
    },

    switchAdminTab(tab) {
        const viewEsc = document.getElementById('admin-view-escenarios');
        const viewRes = document.getElementById('admin-view-reservas');
        const btnEsc = document.getElementById('tab-btn-escenarios');
        const btnRes = document.getElementById('tab-btn-reservas');

        if (tab === 'escenarios') {
            if (viewEsc) viewEsc.style.display = 'block';
            if (viewRes) viewRes.style.display = 'none';
            if (btnEsc) btnEsc.classList.add('active');
            if (btnRes) btnRes.classList.remove('active');
            this.cargarTablaAdmin();
        } else {
            if (viewEsc) viewEsc.style.display = 'none';
            if (viewRes) viewRes.style.display = 'block';
            if (btnEsc) btnEsc.classList.remove('active');
            if (btnRes) btnRes.classList.add('active');
            this.cargarReservasGlobalesAdmin();
        }
    },

    async cargarReservasGlobalesAdmin() {
        if (!this.token) return;
        const tbody = document.getElementById('admin-reservas-tbody');
        const countBadge = document.getElementById('count-global-reservas');
        if (!tbody) return;

        try {
            const res = await fetch(`${API_BASE}/reservas`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });
            if (!res.ok) return;
            const reservas = await res.json();
            if (countBadge) countBadge.textContent = reservas.length;

            if (reservas.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:24px; color:var(--text-muted);">No hay reservas registradas en el sistema.</td></tr>`;
                return;
            }

            tbody.innerHTML = reservas.map(r => {
                const totalFormatted = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(r.precioTotal);
                const esConfirmada = r.estado === 'CONFIRMADA';
                const usuarioNombre = r.usuario ? `${r.usuario.nombre} ${r.usuario.apellido}` : (r.usuarioNombre || 'Deportista');
                const usuarioEmail = r.usuario ? r.usuario.email : (r.usuarioEmail || '');

                return `
                    <tr>
                        <td><code style="color:var(--accent); font-weight:700;">${r.codigoReserva}</code></td>
                        <td>
                            <b>${usuarioNombre}</b>
                            <div style="font-size:0.75rem; color:var(--text-muted);">${usuarioEmail}</div>
                        </td>
                        <td>${r.escenarioNombre}</td>
                        <td>${r.fecha}</td>
                        <td><b>${r.horaInicio} - ${r.horaFin}</b></td>
                        <td style="font-weight:700; color:var(--primary-dark);">${totalFormatted}</td>
                        <td>
                            <span class="badge ${esConfirmada ? 'badge-primary' : 'badge-mantenimiento'}" style="position:static;">
                                ${r.estado}
                            </span>
                        </td>
                        <td>
                            ${esConfirmada ? `
                                <button class="btn btn-danger btn-sm" onclick="app.cancelarReservaAdmin(${r.id})" title="Cancelar reserva por mantenimiento">
                                    <i class="fa-solid fa-ban"></i> Cancelar
                                </button>
                            ` : `
                                <span style="font-size:0.75rem; color:var(--text-muted);">${r.motivoCancelacion || 'Cancelada'}</span>
                            `}
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            console.error('Error cargando reservas globales admin:', err);
        }
    },

    async cancelarReservaAdmin(id) {
        const motivo = prompt('Ingresa el motivo administrativo de la cancelación (ej. Mantenimiento extraordinario):', 'Mantenimiento preventivo de escenario');
        if (!motivo) return;

        try {
            const res = await fetch(`${API_BASE}/reservas/${id}/cancelar`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ motivo })
            });

            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.message || 'Error al cancelar la reserva');
            }

            this.showToast('Reserva cancelada correctamente por administración.', 'info');
            this.cargarReservasGlobalesAdmin();
            this.cargarEscenarios();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    openModalCrearEscenario() {
        document.getElementById('modal-escenario-title').textContent = 'Registrar Nuevo Escenario Deportivo';
        document.getElementById('esc-id').value = '';
        document.getElementById('form-escenario').reset();
        document.getElementById('modal-escenario').style.display = 'flex';
    },

    abrirModalEditarEscenario(id) {
        const esc = this.escenarios.find(e => e.id === id);
        if (!esc) return;

        document.getElementById('modal-escenario-title').textContent = 'Editar Escenario Deportivo';
        document.getElementById('esc-id').value = esc.id;
        document.getElementById('esc-nombre').value = esc.nombre;
        document.getElementById('esc-disciplina').value = esc.disciplina;
        document.getElementById('esc-superficie').value = esc.superficie;
        document.getElementById('esc-aforo').value = esc.aforo;
        document.getElementById('esc-tarifa').value = esc.tarifaHora;
        document.getElementById('esc-imagen').value = esc.imagenUrl || '';
        document.getElementById('esc-desc').value = esc.descripcion || '';
        document.getElementById('esc-luz').checked = esc.iluminacion;
        document.getElementById('esc-mantenimiento').checked = esc.enMantenimiento;

        document.getElementById('modal-escenario').style.display = 'flex';
    },

    async guardarEscenario(e) {
        e.preventDefault();
        const id = document.getElementById('esc-id').value;
        const body = {
            nombre: document.getElementById('esc-nombre').value,
            disciplina: document.getElementById('esc-disciplina').value,
            superficie: document.getElementById('esc-superficie').value,
            aforo: parseInt(document.getElementById('esc-aforo').value),
            tarifaHora: parseFloat(document.getElementById('esc-tarifa').value),
            imagenUrl: document.getElementById('esc-imagen').value,
            descripcion: document.getElementById('esc-desc').value,
            iluminacion: document.getElementById('esc-luz').checked,
            enMantenimiento: document.getElementById('esc-mantenimiento').checked
        };

        const esEdicion = Boolean(id);
        const url = esEdicion ? `${API_BASE}/escenarios/${id}` : `${API_BASE}/escenarios`;
        const method = esEdicion ? 'PUT' : 'POST';

        try {
            const res = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify(body)
            });

            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.message || 'Error al guardar escenario');
            }

            this.closeModal('modal-escenario');
            this.showToast(`Escenario ${esEdicion ? 'actualizado' : 'registrado'} exitosamente`, 'success');
            this.cargarEscenarios();
            this.cargarTablaAdmin();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async toggleMantenimiento(id, nuevoEstado) {
        try {
            const res = await fetch(`${API_BASE}/escenarios/${id}/mantenimiento?enMantenimiento=${nuevoEstado}`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${this.token}` }
            });
            if (!res.ok) throw new Error('Error al actualizar mantenimiento');

            this.showToast(`Estado de mantenimiento ${nuevoEstado ? 'activado' : 'desactivado'}.`, 'info');
            this.cargarEscenarios();
            this.cargarTablaAdmin();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async eliminarEscenario(id) {
        if (!confirm('¿Estás seguro de que deseas eliminar este escenario deportivo?')) return;

        try {
            const res = await fetch(`${API_BASE}/escenarios/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${this.token}` }
            });
            if (!res.ok) throw new Error('Error al eliminar escenario');

            this.showToast('Escenario eliminado correctamente.', 'info');
            this.cargarEscenarios();
            this.cargarTablaAdmin();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // -------------------------------------------------------------
    // UTILIDADES
    // -------------------------------------------------------------
    closeModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
    },

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        const icon = type === 'success' ? 'fa-circle-check' : (type === 'error' ? 'fa-circle-xmark' : 'fa-circle-info');
        toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
        
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

// Iniciar aplicación al cargar el DOM
document.addEventListener('DOMContentLoaded', () => app.init());
