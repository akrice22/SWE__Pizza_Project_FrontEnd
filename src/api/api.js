const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";


export async function getMenu() {
    const response = await fetch(`${API_URL}/menu`);

    if (!response.ok) {
        throw new Error("Failed to fetch menu");
    }

    return response.json();
}


export async function getCustomerByPhone(phoneNumber) {
    const response = await fetch(
        `${API_URL}/customers/${encodeURIComponent(phoneNumber)}`
    );

    if (response.status === 404) {
        return null;
    }

    if (!response.ok) {
        throw new Error("Failed to retrieve customer");
    }

    return response.json();
}


export async function createCustomer(customerData) {
    const response = await fetch(`${API_URL}/customers`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(customerData)
    });

    if (!response.ok) {
        throw new Error("Failed to create customer");
    }

    return response.json();
}