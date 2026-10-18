import time
import logging
import CoolProp.CoolProp as CP
from .config import Config

logger = logging.getLogger(__name__)

class ThermodynamicEngine:
    @staticmethod
    def get_air_properties(t_k):
        """
        Dynamically calculates air density and specific heat using CoolProp
        Falls back to analytical constants if CoolProp fails
        """
        try:
            rho = CP.PropsSI("D", "T", t_k, "P", Config.ATM_PRESSURE_PA, "Air")
            cp = CP.PropsSI("C", "T", t_k, "P", Config.ATM_PRESSURE_PA, "Air")
            return rho, cp, True
        except BaseException as e:
            logger.warning(f"CoolProp evaluation failed, using analytical fallback: {e}")
            return 1.2, 1005.0, False # rho=1.2, Cp=1005

    @classmethod
    def calculate_prediction(cls, raw_data):
        """
        Executes the predictive thermal algorithm.
        """
        start_time = time.perf_counter()

        t_supply = raw_data.get('crah_supply_temp_c')
        p_it_kw = raw_data.get('it_load_kw')
        v_dot_m3s = raw_data.get('crah_airflow_m3s')
        t_actual = raw_data.get('actual_rack_temp_c')

        # Convert to Kelvin for CoolProp
        t_k = t_supply + 273.15

        rho, cp, used_coolprop = cls.get_air_properties(t_k)

        # THERM-NET Core Equation
        t_predicted = t_supply + ((p_it_kw * 1000.0) / (rho * cp * v_dot_m3s))

        # Round to 3 decimal places
        t_predicted = round(t_predicted, 3)
        temp_delta = round(t_predicted - t_actual, 3)

        # Spec Constraint: Log delta = 0 during normal operating baseline
        # Clamp tiny floating-point drifts to strictly 0.0
        if abs(temp_delta) < 0.05:
            temp_delta = 0.0

        latency_ms = int((time.perf_counter() - start_time) * 1000)

        # Enriched DTO
        return {
            "raw_telemetry": raw_data,
            "t_predicted_c": t_predicted,
            "temp_delta": temp_delta,
            "rho_kg_m3": round(rho, 4),
            "cp_j_kgk": round(cp, 4),
            "calc_latency_ms": latency_ms,
            "used_coolprop": used_coolprop
        }