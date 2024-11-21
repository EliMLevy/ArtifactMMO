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
@click.option('--subaction', help='Optional subaction for the default action')
def set_default(character, action, subaction):
    """Set the default action for a character"""
    try:
        payload = {
            "character": character,
            "action": action
        }
        if subaction:
            payload["subaction"] = subaction
        
        response = requests.post(
            f"{BASE_URL}/setdefault", 
            json=payload
        )
        response.raise_for_status()
        
        if subaction:
            click.echo(f"Set default action for {character} to {action} with subaction {subaction}")
        else:
            click.echo(f"Set default action for {character} to {action}")
    except requests.RequestException as e:
        click.echo(f"Error setting default action: {e}", err=True)

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