async function loadMonster(id) {
    try {
        const response = await fetch(`https://www.dnd5eapi.co/api/2014/monsters/${id}`);
        if(!response.ok) {
            console.error('somethings wrong', response.status);
            return null;
        }
        return await response.json();

    } catch (networkError) {
        console.error('Networkj Failure', networkError);
        return null;
    }
}

loadMonster('owlbear');