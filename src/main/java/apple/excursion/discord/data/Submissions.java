package apple.excursion.discord.data;

import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Submissions {
    private static Map<Long, Submission> submissions = new HashMap<>(); // messageId to submission
    private static final Object syncSubmissions = new Object();

    public static void addSubmission(Message message, List<Pair<Long, String>> idToName, List<Message> reviewers,
                                     String questName, List<String> links, List<Message.Attachment> attachment) {
        synchronized (syncSubmissions) {
            submissions.put(message.getIdLong(), new Submission(message, idToName, reviewers, questName, links, attachment));
        }
    }

    public static void markAsDone(Long messageId, User user, boolean isAccepted) {
        synchronized (syncSubmissions) {
            for (Long submissionId : submissions.keySet()) {
                if (messageId.equals(submissionId)) {
                    // we found the right submission

                    Submission submission = submissions.get(submissionId);
                    if (isAccepted)
                        submission.completeSubmit();
                    submission.confirm(isAccepted);

                    // find all the messages sent to other reviewers
                    for (Message others : submission.reviewersMessages) {
                        if (isAccepted)
                            others.editMessage("**" + user.getName() + " accepted the submission**\n" + others.getContentStripped()).queue();
                        else
                            others.editMessage("**" + user.getName() + " denied the submission**\n" + others.getContentStripped()).queue();

                        others.removeReaction("\u274C").queue();
                        others.removeReaction("\u2705").queue();
                        submissions.remove(others.getIdLong());
                    }
                    submissions.remove(submissionId);
                    break;
                }
            }
        }
    }

}
