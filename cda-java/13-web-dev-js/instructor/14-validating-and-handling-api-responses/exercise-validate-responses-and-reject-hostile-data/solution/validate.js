function isJsonContentType(content){
    return typeof content === 'string' && content.includes('application/json');
}

function validateOrder(order) {
    if (typeof order !== 'object' || order === null) return false;
    if (typeof order.customer !== 'object' || order.customer === null) return false;
    if (typeof order.customer.name !== 'string') return false;
    if (typeof order.total != 'number') return false;
    return true;
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {isJsonContentType, validateOrder};
}
