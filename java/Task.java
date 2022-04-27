import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * The Task class contains the main() method and
 * everything else required for the Task CLI.
 *
 * @author Swastik Pal
 * @since 02/12/2001 (DD/MM/YYYY)
 */
public class Task
{
	private static String filename = "task.txt";
	private static String completedFilename = "completed.txt";

	/**
	 * Shows the help text.
	 */
	private static void help()
	{
		String usage = "Usage :-\n" +
				"$ ./task add 2 hello world    # Add a new item with priority 2 and text \"hello world\" to the list\n" +
				"$ ./task ls                   # Show incomplete priority list items sorted by priority in ascending order\n" +
				"$ ./task del INDEX            # Delete the incomplete item with the given index\n" +
				"$ ./task done INDEX           # Mark the incomplete item with the given index as complete\n" +
				"$ ./task help                 # Show usage\n" +
				"$ ./task report               # Statistics\n";
		System.out.print(usage);
	}

	/**
	 * Shows incomplete priority list items sorted by priority in ascending order
	 */
	private static void ls()
	{
		try (Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))))
		{
			int priority;
			String task;
			LinkedList<TaskItem> list = new LinkedList<>();
			while (inFile.hasNext())
			{
				priority = inFile.nextInt();
				task = inFile.nextLine().trim();
				list.add(new TaskItem(task, priority));
			}
//			Collections.sort(list);
			for (int i = 1; list.size() > 0; i++)
			{
				TaskItem item = list.remove();
				System.out.print(i + ". " + item.taskText + " [" + item.priority + "]\n");
			}
		} catch (FileNotFoundException e)
		{
			System.out.print("There are no pending tasks!\n");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new task to the file. The tasks in the file are maintained in ascending order.
	 *
	 * @param priority the priority of the task
	 * @param taskText the task description
	 */
	private static void add(int priority, String taskText)
	{
		LinkedList<TaskItem> list = new LinkedList<>();
		int index = 0;
		try (Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))))
		{
			int p;
			String task;
			while (inFile.hasNext())
			{
				p = inFile.nextInt();
				task = inFile.nextLine().trim();
				list.add(new TaskItem(task, p));
				if (p <= priority)
					++index;
			}
//			Collections.sort(list);
		} catch (FileNotFoundException ignored)
		{
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			Files.deleteIfExists(Path.of(filename));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try (PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))))
		{
			list.add(index, new TaskItem(taskText, priority));
			for (TaskItem item : list)
				outFile.print(item + "\n");
			System.out.print("Added task: \"" + taskText + "\" with priority " + priority + "\n");
			outFile.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Delete the incomplete item with the given index
	 *
	 * @param index the index of the item to delete
	 */
	private static void del(int index)
	{
		try (Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))))
		{
			int priority;
			String task;
			LinkedList<TaskItem> list = new LinkedList<>();
			while (inFile.hasNext())
			{
				priority = inFile.nextInt();
				task = inFile.nextLine().trim();
				list.add(new TaskItem(task, priority));
			}
			if (index >= 1 && index <= list.size())
			{
				list.remove(index - 1);
				System.out.print("Deleted task #" + index + "\n");
			} else
			{
				System.out.print("Error: task with index #" + index + " does not exist. Nothing deleted.\n");
			}
			inFile.close();
			try (PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8, false))))
			{
				for (TaskItem item : list)
					outFile.print(item + "\n");
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} catch (FileNotFoundException e)
		{
			System.out.print("Error: task with index #" + index + " does not exist. Nothing deleted.\n");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Marks the incomplete item with the given index as complete
	 *
	 * @param index the index of the item to mark as complete
	 */
	private static void done(int index)
	{
		try (Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))))
		{
			int priority;
			String task;
			LinkedList<TaskItem> list = new LinkedList<>();
			while (inFile.hasNext())
			{
				priority = inFile.nextInt();
				task = inFile.nextLine().trim();
				list.add(new TaskItem(task, priority));
			}
			TaskItem completed = null;
			Collections.sort(list);
			if (index >= 1 && index <= list.size())
			{
				completed = list.remove(index - 1);
				System.out.print("Marked item as done.\n");
			} else
			{
				System.out.print("Error: no incomplete item with index #" + index + " exists.\n");
			}
			inFile.close();
			if (completed != null)
			{
				try (PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8, false))))
				{
					for (TaskItem item : list)
						outFile.print(item + "\n");
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				try (PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(completedFilename, StandardCharsets.UTF_8, true))))
				{
					outFile.print(completed + "\n");
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e)
		{
			System.out.print("Error: item with index " + index + " does not exist. Nothing deleted.\n");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Shows two groups listing the complete and incomplete tasks.
	 */
	private static void report()
	{
		try (Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))))
		{
			int priority;
			String task;
			LinkedList<TaskItem> list = new LinkedList<>();
			while (inFile.hasNext())
			{
				priority = inFile.nextInt();
				task = inFile.nextLine().trim().trim();
				list.add(new TaskItem(task, priority));
			}
			Collections.sort(list);
			System.out.print("Pending : " + list.size() + "\n");
			for (int i = 1; list.size() > 0; i++)
			{
				TaskItem item = list.remove();
				System.out.print(i + ". " + item.taskText + " [" + item.priority + "]\n");
			}
		} catch (FileNotFoundException e)
		{
			System.out.print("Pending : 0\n");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try (Scanner inFile = new Scanner(new BufferedReader(new FileReader(completedFilename, StandardCharsets.UTF_8))))
		{
			int priority;
			String task;
			LinkedList<TaskItem> list = new LinkedList<>();
			while (inFile.hasNext())
			{
				priority = inFile.nextInt();
				task = inFile.nextLine().trim();
				list.add(new TaskItem(task, priority));
			}
			Collections.sort(list);
			System.out.print("\nCompleted : " + list.size() + "\n");
			for (int i = 1; list.size() > 0; i++)
			{
				TaskItem item = list.remove();
				System.out.print(i + ". " + item.taskText + "\n");
			}
		} catch (FileNotFoundException e)
		{
			System.out.print("\nCompleted : 0\n");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		String workingDir = System.getProperty("user.dir");
		filename = workingDir + File.separator + filename;
		completedFilename = workingDir + File.separator + completedFilename;
		if (args == null || args.length < 1) help();
		else
		{
			String command = args[0];
			if (command.trim().equalsIgnoreCase(commands.add.toString()))
			{
//				System.err.println(Arrays.toString(args)); //for debugging
				if (args.length >= 3)
				{
					add(Integer.parseInt(args[1]), args[2]);
//				} else if (args.length == 2) {
//					add(Integer.parseInt(args[1].substring(0, args[1].indexOf("\""))), args[1].substring(args[1].indexOf("\""), args[1].lastIndexOf("\"")));
				} else System.out.println("Error: Missing tasks string. Nothing added!");
			} else if (command.trim().equalsIgnoreCase(commands.ls.toString()))
			{
				ls();
			} else if (command.trim().equalsIgnoreCase(commands.del.toString()))
			{
				if (args.length >= 2)
				{
					del(Integer.parseInt(args[1]));
				} else System.out.println("Error: Missing NUMBER for deleting tasks.");
			} else if (command.trim().equalsIgnoreCase(commands.done.toString()))
			{
				if (args.length >= 2)
				{
					done(Integer.parseInt(args[1]));
				} else System.out.println("Error: Missing NUMBER for marking tasks as done.");
			} else if (command.trim().equalsIgnoreCase(commands.help.toString()))
			{
				help();
			} else if (command.trim().equalsIgnoreCase(commands.report.toString()))
			{
				report();
			}
		}
//		System.out.println("Hello, World!");
	}

	public enum commands
	{
		add, ls, del, done, help, report
	}

	/**
	 * A TaskItem object has two attributes
	 * <ul>
	 * <li> int priority - the priority of the task</li>
	 * <li> String taskText - the task description</li>
	 * </ul>
	 * TaskItems are Comparable. They are compared on the basis of their priorities.
	 *
	 * @author Swastik Pal
	 */
	static class TaskItem implements Comparable<TaskItem>
	{
		int priority;
		String taskText;

		public TaskItem(String taskText, Integer priority)
		{
			this.taskText = taskText;
			this.priority = priority;
		}

		/**
		 * Compares this TaskItem with the specified TaskItem for order.  Returns
		 * a negative integer, zero, or a positive integer as this object is less
		 * than, equal to, or greater than the specified object.
		 *
		 * <p>The implementor must ensure
		 * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
		 * for all {@code x} and {@code y}.  (This
		 * implies that {@code x.compareTo(y)} must throw an exception iff
		 * {@code y.compareTo(x)} throws an exception.)
		 *
		 * <p>The implementor must also ensure that the relation is transitive:
		 * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
		 * {@code x.compareTo(z) > 0}.
		 *
		 * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
		 * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
		 * all {@code z}.
		 *
		 * <p>It is strongly recommended, but <i>not</i> strictly required that
		 * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
		 * class that implements the {@code Comparable} interface and violates
		 * this condition should clearly indicate this fact.  The recommended
		 * language is "Note: this class has a natural ordering that is
		 * inconsistent with equals."
		 *
		 * <p>In the foregoing description, the notation
		 * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
		 * <i>signum</i> function, which is defined to return one of {@code -1},
		 * {@code 0}, or {@code 1} according to whether the value of
		 * <i>expression</i> is negative, zero, or positive, respectively.
		 *
		 * @param o the object to be compared.
		 * @return a negative integer, zero, or a positive integer as this object
		 * is less than, equal to, or greater than the specified object.
		 * @throws NullPointerException if the specified object is null
		 * @throws ClassCastException   if the specified object's type prevents it
		 *                              from being compared to this object.
		 */
		@Override
		public int compareTo(TaskItem o)
		{
			return (this.priority - o.priority);
		}

		@Override
		public String toString()
		{
			return priority + " " + taskText;
		}
	}

}
