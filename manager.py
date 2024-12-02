from Character import Character


JOE = "Joe"


class Manager():
    def __init__(self) -> None:
        self.characters = {}
        self.characters[JOE] = Character(JOE)
        self.weapon_crafter = JOE

        pass

    def improve_weaponcrafting(self):
        '''
        Gather craftables from most recent level unlocked
        Filter out expensive items (jasper-crystals)
        '''
        pass