package bms.player.beatoraja.ir;

import bms.player.beatoraja.IRScoreData;
import bms.player.beatoraja.PlayerInformation;
import bms.player.beatoraja.TableData;
import bms.player.beatoraja.CourseData;
import bms.player.beatoraja.song.SongInformation;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.play.JudgeProperty;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import xyz.seraphin.discordir.IRServiceGrpc.IRServiceBlockingStub;
import xyz.seraphin.discordir.IRServiceGrpc;
import xyz.seraphin.discordir.DiscordIRService.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ManagedChannel;

public class DiscordIRConnection implements IRConnection {
    public static final String NAME = "Rennaisance Discord IR";
    public static final String HOME = "https://seraphin.xyz/";
    public static final String VERSION = "1.0.2";

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
        LoginMessage r = LoginMessage.newBuilder().setId(id).setPass(pass).setVersion(this.VERSION).build();
        Token token;
        try {
            token = this.stub.login(r);
        } catch (Exception e) {
            e.printStackTrace();
            return rc.create(false, "GRPC exception: " + e.getMessage(), null);
        }
        if(!token.getSuccess()) return rc.create(false, token.getToken(), null);
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
        
        SongInfo.Builder si = SongInfo.newBuilder().setMd5(model.getMd5()).setSha256(model.getSha256());
        si.setNotes(model.getNotes()).setTitle(model.getFullTitle()).setArtist(model.getFullArtist()).setGenre(model.getGenre()).setJudge(model.getJudge());
        si.setBpm(model.getInformation().getMainbpm()).setTotal(model.getInformation().getTotal()).setAvgdensity(model.getInformation().getDensity()).setPeakdensity(model.getInformation().getPeakdensity()).setEnddensity(model.getInformation().getEnddensity());
        
        List<Integer> fast = new ArrayList<>();
        List<Integer> slow = new ArrayList<>();
        List<Integer> hit = new ArrayList<>();
        for(int i = 0; i <= 5; i++) {
            fast.add(score.getJudgeCount(i, true));
            slow.add(score.getJudgeCount(i, false));
            hit.add(score.getJudgeCount(i, true)+score.getJudgeCount(i, false));
        }

        Result.Builder r = Result.newBuilder().setToken(this.token).setInfo(si.build()).addAllHit(hit).addAllFast(fast).addAllSlow(slow).setClear(score.getClear());
        r.setLr2Oraja(Arrays.stream(JudgeProperty.class.getFields()).anyMatch(p -> p.getName().equals("LR2_JUDGE_WINDOWS")));
        r.setCombo(score.getCombo()).setBp(score.getMinbp()).setRandom(score.getRandom());
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
