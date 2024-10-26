import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Repo {
    private File repo;
    private PrintWriter writer;
    private String[] content;

    public Repo(String repoName) {
       repo = new File("./" + repoName);
        try {
            writer = new PrintWriter(repo);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + repoName);
        }
    }


    public ArrayList<String> getSearchContent() {
        ArrayList<String> list = new ArrayList<>();
        for (String title : repo.list()) {
            list.add(title);
        }
        return list;
    }

    public String listToString(ArrayList<String> list) {
        String result = "";
        for(String str : list) {
            result += str + ",";
        }
        return result;
    }



    public static void main(String[] args) {
        Repo repo = new Repo("dll1");
    }
}
