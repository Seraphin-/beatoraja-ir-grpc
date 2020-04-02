package bms.player.beatoraja.ir;

import bms.player.beatoraja.IRScoreData;
import bms.player.beatoraja.PlayerInformation;
import bms.player.beatoraja.TableData;
import bms.player.beatoraja.CourseData;
import bms.player.beatoraja.song.SongInformation;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.play.JudgeProperty;
import java.util.Arrays;
import xyz.seraphin.discordir.IRServiceGrpc.IRServiceBlockingStub;
import xyz.seraphin.discordir.IRServiceGrpc;
import xyz.seraphin.discordir.DiscordIRService.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ManagedChannel;

public class DiscordIRConnection implements IRConnection {
    public static final String NAME = "Rennaisance Discord IR";
    public static final String HOME = "https://seraphin.xyz/";

    private static final String GRPC_URL = "seraphin.xyz";
    private static final int GRPC_PORT = 20777;

    private String token = "";
    private ManagedChannel channel;
    private IRServiceBlockingStub stub;

    public DiscordIRConnection() {
        this.channel = ManagedChannelBuilder.forAddress(this.GRPC_URL, this.GRPC_PORT).build();
        this.stub = IRServiceGrpc.newBlockingStub(channel);
    }

    public IRResponse<Object> register(String id, String pass, String name) { return null; }

    public IRResponse<Object> login(String id, String pass) {
        ResponseCreator<Object> rc = new ResponseCreator<Object>();
        LoginMessage r = LoginMessage.newBuilder().setId(id).setPass(pass).build();
        Token token;
        try {
            token = this.stub.login(r);
        } catch (Exception e) {
            e.printStackTrace();
            return rc.create(false, "GRPC exception: " + e.getMessage(), null);
        }
        if(!token.getSuccess()) return rc.create(false, "Invalid user/password combination", null);
        this.token = token.getToken();
        return rc.create(true, "Logged in", null);
    }

    public IRResponse<PlayerInformation[]> getRivals() {
        ResponseCreator<PlayerInformation[]> rc = new ResponseCreator<PlayerInformation[]>();
        return rc.create(false, "Not implemented", new PlayerInformation[0]);
    }
    public IRResponse<TableData[]> getTableDatas() {
        ResponseCreator<TableData[]> rc = new ResponseCreator<TableData[]>();
        return rc.create(false, "Not implemented", new TableData[0]);
    }
    public IRResponse<IRScoreData[]> getPlayData(String id, SongData model) {
        ResponseCreator<IRScoreData[]> rc = new ResponseCreator<IRScoreData[]>();
        return rc.create(false, "Not implemented", new IRScoreData[0]);
    }
    public IRResponse<IRScoreData[]> getCoursePlayData(String id, CourseData course, int lnmode) {
        ResponseCreator<IRScoreData[]> rc = new ResponseCreator<IRScoreData[]>();
        return rc.create(false, "Not implemented", new IRScoreData[0]);
    }
    public IRResponse<Object> sendPlayData(SongData model, IRScoreData score) {
        ResponseCreator<Object> rc = new ResponseCreator<Object>();
        Result.Builder r = Result.newBuilder().setToken(this.token);
        r.setMd5(model.getMd5()).setSha256(model.getSha256()).setClear(score.getClear());
        r.setLr2Oraja(Arrays.stream(JudgeProperty.class.getFields()).anyMatch(p -> p.getName().equals("LR2_JUDGE_WINDOWS")));
        r.setPgreat(score.getJudgeCount(0, true)+score.getJudgeCount(0, false));
        r.setGreat(score.getJudgeCount(1, true)+score.getJudgeCount(1, false));
        r.setGood(score.getJudgeCount(2, true)+score.getJudgeCount(2, false));
        r.setBad(score.getJudgeCount(3, true)+score.getJudgeCount(3, false));
        r.setMashpoor(score.getJudgeCount(4, true)+score.getJudgeCount(4, false));
        r.setMiss(score.getJudgeCount(5, true)+score.getJudgeCount(5, false));
        r.setCombo(score.getCombo()).setBp(score.getMinbp()).setRandom(score.getRandom());
        int fast = 0;
        int slow = 0;
        for(int i = 0; i <= 5; i++) {
            fast += score.getJudgeCount(i, true);
            slow += score.getJudgeCount(i, false);
        }
        r.setFast(fast).setSlow(slow).setNotes(model.getNotes()).setTitle(model.getFullTitle()).setArtist(model.getFullArtist()).setGenre(model.getGenre()).setJudge(model.getJudge());
        r.setBpm(model.getInformation().getMainbpm()).setTotal(model.getInformation().getTotal()).setAvgdensity(model.getInformation().getDensity()).setPeakdensity(model.getInformation().getDensity()).setEnddensity(model.getInformation().getEnddensity());
        Success s;
        try {
            s = this.stub.sendResult(r.build());
        } catch (Exception e) {
            e.printStackTrace();
            return rc.create(false, "GRPC exception: " + e.getMessage(), null);
        }
        return rc.create(s.getSuccess(), "Got status (if any): " + s.getStatus(), null);
    }
    public IRResponse<Object> sendCoursePlayData(CourseData course, int lnmode, IRScoreData score) {
        ResponseCreator<Object> rc = new ResponseCreator<Object>();
        return rc.create(false, "Not implemented", null);
    }
    public String getSongURL(SongData song) { return null; }
    public String getCourseURL(CourseData course) { return null; }
    public String getPlayerURL(String id) { return "https://seraphin.xyz/"; }
}
