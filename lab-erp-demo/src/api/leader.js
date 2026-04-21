import request from '@/utils/request'

export function getLeaderDashboard(role) {
    return request.get('/api/leader/dashboard', { params: { role } })
}

export function checkLeaderStatus(role) {
    return request.get('/api/leader/check-leader', { params: { role } })
}

export function getCurrentLeader(role) {
    return request.get('/api/leader/current-leader', { params: { role } })
}

export function assignRole(data) {
    return request.post('/api/leader/assign-role', data)
}

export function removeRole(data) {
    return request.delete('/api/leader/remove-role', { data })
}

export function getAllUsers() {
    return request.get('/api/users')
}
