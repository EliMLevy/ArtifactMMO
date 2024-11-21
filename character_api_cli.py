import json
import click
import requests

BASE_URL = "http://localhost:5000"

@click.group()
def cli():
    """CLI tool for interacting with the Character API"""
    pass

@cli.command()
@click.argument('character')
@click.argument('plan', nargs=-1)
def submit_plan(character, plan):
    """Submit a plan for a specific character"""
    try:
        response = requests.post(
            f"{BASE_URL}/submitplan", 
            json={
                "character": character,
                "plan": list(plan)
            }
        )
        response.raise_for_status()
        click.echo(f"Plan for {character} submitted successfully: {plan}")
    except requests.RequestException as e:
        click.echo(f"Error submitting plan: {e}", err=True)

@cli.command()
@click.argument('character')
@click.argument('action')
@click.argument('subaction', required=False)
def set_default(character, action, subaction):
    """Set the default action for a character"""
    try:
        payload = {
            "character": character,
            "action": action
        }
        
        # Parse subaction if provided
        if subaction:
            try:
                # Try to parse as JSON first
                subaction = subaction.replace("'", '"')
                parsed_subaction = json.loads(subaction)
                payload["subaction"] = parsed_subaction
            except json.JSONDecodeError:
                # If not JSON, treat as a simple string
                payload["subaction"] = subaction
        print(payload)
        response = requests.post(
            f"{BASE_URL}/setdefault", 
            json=payload
        )
        response.raise_for_status()
        
        click.echo(f"Set default action for {character} to {action}")
        if subaction:
            click.echo(f"Subaction: {subaction}")
    except requests.RequestException as e:
        click.echo(f"Error setting default action: {e}", err=True)

# ... (rest of the previous code remains the same)

if __name__ == '__main__':
    cli()
@cli.command()
@click.argument('character')
def empty_plan(character):
    """Empty the plan for a specific character"""
    try:
        response = requests.post(
            f"{BASE_URL}/emptyplan", 
            json={"character": character}
        )
        response.raise_for_status()
        click.echo(f"Plan for {character} emptied successfully")
    except requests.RequestException as e:
        click.echo(f"Error emptying plan: {e}", err=True)

if __name__ == '__main__':
    cli()


'''
python character_api_cli.py set-default Tim craft "{'code':'iron','quantity':12}"
python character_api_cli.py set-default Bobby attack cow

'''