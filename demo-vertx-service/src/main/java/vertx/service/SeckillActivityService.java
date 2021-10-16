package vertx.service;

import com.demo.support.dto.Result;
import com.demo.support.dto.SeckillActivityDTO;

public interface SeckillActivityService {

    Result<Integer> createActivity(SeckillActivityDTO activityDTO);

}
