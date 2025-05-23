from kivymd.toast import toast
from functools import partial
from kivy.lang import Builder
from kivymd.app import MDApp
from kivymd.uix.screen import MDScreen
from kivymd.uix.button import MDRaisedButton
from kivymd.uix.label import MDLabel
from kivymd.uix.textfield import MDTextField
from kivymd.uix.list import OneLineListItem
from kivymd.uix.screenmanager import MDScreenManager
from datetime import datetime
import calendar
from kivy.metrics import dp
from kivy.clock import Clock
from kivymd.uix.boxlayout import MDBoxLayout  # Fix missing import for MDBoxLayout
from kivy.uix.boxlayout import BoxLayout  # Fix missing import for BoxLayout
import sqlite3


kv = """
MDScreenManager:
    DashboardScreen:
    TaskScreen:
    CalendarScreen:

<DashboardScreen>:
    name: 'dashboard'
    MDBoxLayout:
        orientation: 'vertical'
        spacing: dp(20)
        padding: dp(20)

        MDLabel:
            id: current_date_label
            text: "Today's Date: "
            halign: 'center'

        MDRaisedButton:
            text: 'Select Date'
            pos_hint: {'center_x': 0.5}
            on_release: app.show_calendar()

        MDRaisedButton:
            text: 'Add Task for Today'
            pos_hint: {'center_x': 0.5}
            on_release: app.show_tasks_today()

        MDLabel:
            id: tasks_today_label
            text: "Tasks Today: "
            halign: 'center'

<TaskScreen>:
    name: 'task'
    MDBoxLayout:
        orientation: 'vertical'
        padding: dp(20)
        spacing: dp(10)

        MDLabel:
            id: task_date_label
            text: 'Tasks for: '
            halign: 'center'
            font_style: 'H5'

        ScrollView:
            MDList:
                id: task_list

        MDTextField:
            id: new_task
            hint_text: 'Enter new task'
            mode: 'rectangle'

        MDRaisedButton:
            text: 'Add Task'
            pos_hint: {'center_x': 0.5}
            on_release: root.add_task()  

        MDRaisedButton:
            text: 'Back to Dashboard'
            pos_hint: {'center_x': 0.5}
            on_release: app.go_back_dashboard()

<CalendarScreen>:
    name: 'calendar'
    MDBoxLayout:
        orientation: 'vertical'
        padding: dp(20)
        spacing: dp(10)

        MDLabel:
            id: month_label
            text: 'Pick a Date'
            halign: 'center'
            font_style: 'H5'

        BoxLayout:
            orientation: 'horizontal'
            size_hint_y: None
            height: dp(40)
            spacing: dp(10)

            MDRaisedButton:
                text: '<'
                on_release: app.change_month(-1)

            MDRaisedButton:
                text: '<<'
                on_release: app.change_year(-1)

            MDLabel:
                id: calendar_month_label
                text: ''  # Set in Python
                halign: 'center'

            MDRaisedButton:
                text: '>>'
                on_release: app.change_year(1)

            MDRaisedButton:
                text: '>'
                on_release: app.change_month(1)

        GridLayout:
            cols: 7
            row_default_height: "48dp"
            padding: dp(10)
            spacing: dp(5)
            id: calendar_grid

        MDRaisedButton:
            text: 'Back to Dashboard'
            pos_hint: {'center_x': 0.5}
            on_release: app.go_back_dashboard()
"""


class TaskDatabase:
    def __init__(self, db_name="tasks.db"):
        """Initialize the SQLite database connection."""
        self.db_name = db_name
        self.connection = sqlite3.connect(self.db_name)
        self.cursor = self.connection.cursor()
        self.create_table()

    def create_table(self):
        """Create a tasks table if it doesn't exist."""
        self.cursor.execute('''
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                task TEXT NOT NULL,
                completed BOOLEAN NOT NULL DEFAULT 0
            )
        ''')
        self.connection.commit()

    def add_task(self, date, task):
        """Add a new task to the database."""
        self.cursor.execute('''
            INSERT INTO tasks (date, task, completed)
            VALUES (?, ?, ?)
        ''', (date, task, False))  # Task is initially not completed
        self.connection.commit()

    def get_tasks_for_date(self, date):
        """Get all tasks for a specific date."""
        self.cursor.execute('''
            SELECT id, task, completed FROM tasks WHERE date = ?
        ''', (date,))
        tasks = self.cursor.fetchall()
        return [{"id": task[0], "task": task[1], "completed": task[2]} for task in tasks]

    def mark_task_completed(self, task_id):
        """Mark a task as completed."""
        self.cursor.execute('''
            UPDATE tasks SET completed = 1 WHERE id = ?
        ''', (task_id,))
        self.connection.commit()

    def delete_task(self, task_id):
        """Delete a task by ID."""
        self.cursor.execute('''
            DELETE FROM tasks WHERE id = ?
        ''', (task_id,))
        self.connection.commit()

    def close(self):
        """Close the database connection."""
        self.connection.close()


class DashboardScreen(MDScreen):
    """Logic for the Dashboard screen"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.selected_date = ""
        self.db = MDApp.get_running_app().db

    def on_enter(self):
        """This method is called when the screen is entered. Here, we can update the UI."""
        Clock.schedule_once(self.update_dashboard_content, 0)

    def update_dashboard_content(self, *args):
        """Update dynamic content (e.g., tasks for today, current date)"""
        today = datetime.now().strftime('%Y-%m-%d')
        tasks_today = self.db.get_tasks_for_date(today)
        if not tasks_today:
            self.ids.tasks_today_label.text = "No tasks for today"
        else:
            self.ids.tasks_today_label.text = f"Tasks Today: {len(tasks_today)}"
        try:
            self.ids.current_date_label.text = f"Today's Date: {today}"
        except AttributeError:
            print("Error: current_date_label not found")
            self.ids.current_date_label.text = "Today's Date: Not Available"

    def show_calendar(self):
        """Navigate to the Calendar Screen"""
        self.parent.current = 'calendar'
        self.parent.get_screen('calendar').update_calendar()

    def show_tasks_today(self):
        """Navigate to the Task Screen for today's tasks"""
        today = datetime.now().strftime('%Y-%m-%d')
        task_screen = self.parent.get_screen('task')
        task_screen.selected_date = today
        task_screen.update_task_screen()
        self.parent.current = 'task'

    def go_back_dashboard(self):
        """Go back to the dashboard screen"""
        self.parent.current = 'dashboard'


class TaskScreen(MDScreen):
    """Logic for task screen where user can add and view tasks."""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.selected_date = ""
        self.db = MDApp.get_running_app().db  # Get the database instance from the app

    def update_task_screen(self):
        """Update the task screen with tasks for the selected date."""
        Clock.schedule_once(self._update_task_screen, 0)

    def _update_task_screen(self, *args):
        """Actually update the task screen"""
        screen = self
        screen.ids.task_date_label.text = f'Tasks for: {self.selected_date}'
        screen.ids.task_list.clear_widgets()

        # Get tasks from the database
        tasks = self.db.get_tasks_for_date(self.selected_date)
        for task in tasks:
            task_item = OneLineListItem(
                text=task["task"],
                on_release=self.toggle_task_completion
            )

            # Apply strike-through if the task is completed
            if task["completed"]:
                task_item.font_style = "Subtitle1"
                task_item.text = f"[s]{task['task']}[/s]"  # Strike-through text

            # Store the task's ID for later deletion or completion
            task_item.task_id = task["id"]

            # Create the delete button and position it to the right side
            delete_button = MDRaisedButton(
                text="Delete",
                size_hint=(None, None),
                size=("80dp", "40dp"),
                pos_hint={"right": 1},  # Position the delete button on the right
                on_release=partial(self.delete_task, task["id"])
            )

            # Create a layout for each task item
            task_layout = MDBoxLayout(orientation='horizontal', size_hint_y=None, height=dp(50))
            task_layout.add_widget(task_item)
            task_layout.add_widget(delete_button)

            screen.ids.task_list.add_widget(task_layout)

    def toggle_task_completion(self, instance):
        """Toggle task completion (strike-through)."""
        if instance.text.startswith("[s]"):  # If task is completed, remove strike-through
            instance.text = instance.text[3:-4]
            self.db.mark_task_completed(instance.task_id)
        else:  # Otherwise, mark it as completed (add strike-through)
            instance.text = f"[s]{instance.text}[/s]"
            self.db.mark_task_completed(instance.task_id)
        self.update_task_screen()

    def add_task(self):
        """Add a new task for the selected date."""
        if not self.selected_date:
            print("Error: No date selected.")
            return
        
        new_task = self.ids.new_task.text.strip()
        if not new_task:
            toast("Please enter a valid task.")
            return
        if new_task:
            self.db.add_task(self.selected_date, new_task)
            toast("Task added successfully!")
            print(f"Task added for {self.selected_date}: {new_task}")
            self.ids.new_task.text = ''
            self.update_task_screen()
        else:
            print("Please enter a valid task.")

    def delete_task(self, task_id):
        """Delete a task by ID."""
        self.db.delete_task(task_id)
        self.update_task_screen()

    def go_back_dashboard(self):
        """Go back to the dashboard screen"""
        self.parent.current = 'dashboard'


class CalendarScreen(MDScreen):
    """Logic for the Calendar screen"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.year = datetime.now().year
        self.month = datetime.now().month
        Clock.schedule_once(self._set_initial_label_text)

    def _set_initial_label_text(self, *args):
        """Set the initial text of the calendar month label."""
        self.ids.calendar_month_label.text = f'{self.get_month_name()} {self.year}'

    def on_pre_enter(self):
        """Update calendar when the screen is about to be entered."""
        self.update_calendar()

    def update_calendar(self):
        """Generate and display the calendar for the current month."""
        month_days = calendar.monthcalendar(self.year, self.month)
        calendar_grid = self.ids.calendar_grid
        calendar_grid.clear_widgets()

        weekdays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        for day in weekdays:
            calendar_grid.add_widget(MDLabel(text=day, halign="center"))

        for week in month_days:
            for day in week:
                if day != 0:
                    btn = MDRaisedButton(
                        text=str(day),
                        size_hint=(None, None),
                        size=("40dp", "40dp"),
                        pos_hint={"center_x": 0.5},
                        on_release=self.select_date,
                    )
                    btn.day = day  # Store the day for reference
                    calendar_grid.add_widget(btn)
                else:
                    calendar_grid.add_widget(MDLabel())  # Add an empty label for empty days

        self.ids.calendar_month_label.text = f'{self.get_month_name()} {self.year}'

    def select_date(self, instance):
        """Select a date and navigate to the task screen"""
        selected_date = f"{self.year}-{self.month:02d}-{instance.day:02d}"
        task_screen = self.parent.get_screen('task')
        task_screen.selected_date = selected_date
        task_screen.update_task_screen()
        self.parent.current = 'task'

    def change_month(self, direction):
        """Change the current month (forward or backward)"""
        new_month = self.month + direction
        if new_month > 12:
            self.month = 1
            self.year += 1
        elif new_month < 1:
            self.month = 12
            self.year -= 1
        else:
            self.month = new_month
        self.update_calendar()

    def change_year(self, direction):
        """Change the current year (forward or backward)"""
        self.year += direction
        if self.year < 1900:  # Assume 1900 is the min year
            self.year = 1900
        elif self.year > 2100:  # Assume 2100 is the max year
            self.year = 2100
        self.update_calendar()

    def get_month_name(self):
        """Get the name of the current month"""
        return calendar.month_name[self.month]

    def go_back_dashboard(self):
        """Go back to the dashboard screen"""
        self.parent.current = 'dashboard'


class CalendarApp(MDApp):
    """Main app class"""

    def build(self):
        self.theme_cls.primary_palette = 'BlueGray'
        self.title = "Task Maker"
        
        # Initialize the database once when the app starts
        self.db = TaskDatabase()  # Initialize the database connection
        return Builder.load_string(kv)

    def show_calendar(self):
        """Display the calendar screen"""
        self.root.current = 'calendar'

    def show_tasks_today(self):
        """Navigate to the Task Screen for the currently selected date (from dashboard)"""
        today = datetime.now().strftime('%Y-%m-%d')
        task_screen = self.root.get_screen('task')
        task_screen.selected_date = today
        task_screen.update_task_screen()
        self.root.current = 'task'

    def go_back_dashboard(self):
        """Go back to the dashboard screen"""
        self.root.current = 'dashboard'

    def on_stop(self):
        """Close the database connection when the app stops"""
        self.db.close()


if __name__ == '__main__':
    CalendarApp().run()
